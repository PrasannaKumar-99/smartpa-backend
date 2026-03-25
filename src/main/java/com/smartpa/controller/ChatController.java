package com.smartpa.controller;

import com.smartpa.dto.DTOs.*;
import com.smartpa.model.ChatHistory;
import com.smartpa.security.JwtUtil;
import com.smartpa.service.ChatService;
import com.smartpa.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/chat") @RequiredArgsConstructor @Slf4j
public class ChatController {

    private final ChatService chatService;
    private final OpenAIService aiService;
    private final JwtUtil jwtUtil;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatResponse>> send(
            @RequestBody ChatRequest req, @RequestHeader("Authorization") String auth) {
        try {
            if (req.getMessage() == null || req.getMessage().isBlank())
                return ResponseEntity.badRequest().body(ApiResponse.error("Message is empty"));
            Long userId = uid(auth);
            log.info("Chat from userId={}: {}", userId, req.getMessage());
            return ResponseEntity.ok(ApiResponse.success("OK", chatService.sendMessage(req.getMessage(), userId)));
        } catch (Exception e) {
            log.error("Chat error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatHistory>>> history(@RequestHeader("Authorization") String auth) {
        try { return ResponseEntity.ok(ApiResponse.success("OK", chatService.getHistory(uid(auth)))); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage())); }
    }

    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> clear(@RequestHeader("Authorization") String auth) {
        try { chatService.clearHistory(uid(auth)); return ResponseEntity.ok(ApiResponse.success("Cleared", null)); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage())); }
    }

    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<SummarizeResponse>> summarize(
            @RequestBody SummarizeRequest req, @RequestHeader("Authorization") String auth) {
        try {
            String prompt = "Summarize the following text in 2-3 concise sentences:\n\n" + req.getText();
            String summary = aiService.chat(prompt, null);
            return ResponseEntity.ok(ApiResponse.success("OK", new SummarizeResponse(summary)));
        } catch (Exception e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    private Long uid(String auth) { return jwtUtil.extractUserId(auth.substring(7)); }
}
