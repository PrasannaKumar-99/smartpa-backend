package com.smartpa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final ObjectMapper mapper = new ObjectMapper();

    private String systemPrompt() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return "You are SmartPA, a helpful AI personal assistant. Today is " + now + ".\n\n"
            + "RULES:\n"
            + "1. For TASK/REMINDER creation requests ONLY, respond with EXACTLY this JSON (no extra text):\n"
            + "   {\"intent\":\"CREATE_TASK\",\"response\":\"Sure! Task created.\",\"task\":{\"title\":\"task name\",\"description\":\"details\",\"reminderTime\":\"\",\"priority\":\"MEDIUM\"}}\n"
            + "   Priority must be HIGH, MEDIUM, or LOW. reminderTime format: 2025-12-31T18:00:00 or empty.\n"
            + "2. For ALL other messages (greetings, questions, jokes, math, general chat) respond with plain text ONLY.\n"
            + "3. NEVER use JSON for normal conversation. NEVER add markdown code blocks.\n"
            + "4. Keep responses concise and friendly.";
    }

    @Async
    public CompletableFuture<String> chatAsync(String userMessage, List<Map<String, String>> history) {
        return CompletableFuture.completedFuture(chat(userMessage, history));
    }

    public String chat(String userMessage, List<Map<String, String>> history) {
        HttpURLConnection conn = null;
        try {
            // Build messages array
            List<Map<String, Object>> messages = new ArrayList<>();

            Map<String, Object> sys = new LinkedHashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt());
            messages.add(sys);

            // Add conversation history (last 6 exchanges)
            if (history != null) {
                int start = Math.max(0, history.size() - 12);
                for (int i = start; i < history.size(); i++) {
                    Map<String, String> h = history.get(i);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("role", h.get("role"));
                    m.put("content", h.get("content"));
                    messages.add(m);
                }
            }

            Map<String, Object> userMsg = new LinkedHashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // Build request body
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("max_tokens", 800);
            body.put("temperature", 0.7);
            body.put("messages", messages);

            String jsonBody = mapper.writeValueAsString(body);
            log.info(">>> Groq request to {} model={}", apiUrl, model);
            log.debug(">>> Body: {}", jsonBody);

            URL url = new URL(apiUrl.trim());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            log.info("<<< Groq status: {}", status);

            InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
            String raw = readStream(is);
            log.debug("<<< Groq raw: {}", raw);

            if (status != 200) {
                try {
                    String errMsg = mapper.readTree(raw).path("error").path("message").asText();
                    if (!errMsg.isBlank()) {
                        log.error("Groq error: {}", errMsg);
                        return "ERROR:" + errMsg;
                    }
                } catch (Exception ignored) {}
                return "ERROR:HTTP " + status;
            }

            JsonNode root = mapper.readTree(raw);
            String content = root.path("choices").get(0).path("message").path("content").asText("").trim();
            log.info("<<< Groq reply ({}chars): {}", content.length(), content.substring(0, Math.min(120, content.length())));
            return content;

        } catch (Exception e) {
            log.error("Groq call failed: {}", e.getMessage(), e);
            return "ERROR:Connection failed - " + e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    public boolean isTaskIntent(String r) {
        if (r == null || r.isBlank() || r.startsWith("ERROR:")) return false;
        String t = r.trim();
        if (!t.startsWith("{")) return false;
        try {
            return "CREATE_TASK".equals(mapper.readTree(t).path("intent").asText(""));
        } catch (Exception e) { return false; }
    }

    public Map<String, String> extractTask(String r) {
        try {
            JsonNode n = mapper.readTree(r.trim());
            String title = n.path("task").path("title").asText("").trim();
            if (title.isEmpty()) return null;
            Map<String, String> t = new HashMap<>();
            t.put("response",     n.path("response").asText("Task created!"));
            t.put("title",        title);
            t.put("description",  n.path("task").path("description").asText(""));
            t.put("reminderTime", n.path("task").path("reminderTime").asText(""));
            t.put("priority",     n.path("task").path("priority").asText("MEDIUM"));
            return t;
        } catch (Exception e) { return null; }
    }

    public String plainResponse(String r) {
        try {
            String resp = mapper.readTree(r.trim()).path("response").asText("");
            return resp.isEmpty() ? r : resp;
        } catch (Exception e) { return r; }
    }
}
