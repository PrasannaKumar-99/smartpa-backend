package com.smartpa.service;

import com.smartpa.dto.DTOs.*;
import com.smartpa.model.*;
import com.smartpa.model.Task.TaskStatus;
import com.smartpa.model.Task.Priority;
import com.smartpa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatHistoryRepository chatRepo;
    private final UserRepository userRepo;
    private final TaskRepository taskRepo;
    private final OpenAIService ai;

    public ChatResponse sendMessage(String message, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, String>> history = buildHistory(userId);
        String aiRaw = ai.chat(message, history);

        // If AI returned an error string, surface it
        if (aiRaw != null && aiRaw.startsWith("ERROR:")) {
            String errMsg = aiRaw.substring(6);
            // Still save to history
            saveHistory(message, errMsg, user);
            return ChatResponse.builder()
                    .message(message).response(errMsg)
                    .timestamp(LocalDateTime.now())
                    .taskCreated(false).intent("ERROR").build();
        }

        boolean taskCreated = false;
        String taskTitle = null;
        String intent = "CHAT";
        String finalResp = aiRaw;

        if (ai.isTaskIntent(aiRaw)) {
            intent = "CREATE_TASK";
            Map<String, String> taskData = ai.extractTask(aiRaw);
            if (taskData != null) {
                try {
                    Priority priority = Priority.MEDIUM;
                    try { priority = Priority.valueOf(taskData.getOrDefault("priority","MEDIUM").toUpperCase()); }
                    catch (Exception ignored) {}

                    Task task = Task.builder()
                            .title(taskData.get("title"))
                            .description(taskData.getOrDefault("description",""))
                            .status(TaskStatus.PENDING)
                            .priority(priority)
                            .user(user).build();

                    String rt = taskData.get("reminderTime");
                    if (rt != null && !rt.isBlank()) {
                        try { task.setReminderTime(LocalDateTime.parse(rt)); }
                        catch (Exception e) { log.warn("Bad reminderTime: {}", rt); }
                    }
                    taskRepo.save(task);
                    taskCreated = true;
                    taskTitle = taskData.get("title");
                    finalResp = taskData.get("response");
                    log.info("Auto-created task: '{}' priority={}", taskTitle, priority);
                } catch (Exception e) {
                    log.error("Task creation failed: {}", e.getMessage());
                    finalResp = ai.plainResponse(aiRaw);
                }
            } else {
                finalResp = ai.plainResponse(aiRaw);
            }
        }

        saveHistory(message, finalResp, user);

        return ChatResponse.builder()
                .message(message).response(finalResp)
                .timestamp(LocalDateTime.now())
                .taskCreated(taskCreated).taskTitle(taskTitle)
                .intent(intent).build();
    }

    private void saveHistory(String msg, String resp, User user) {
        chatRepo.save(ChatHistory.builder().message(msg).response(resp).user(user).build());
    }

    private List<Map<String, String>> buildHistory(Long userId) {
        List<ChatHistory> recent = chatRepo.findTop10ByUserIdOrderByTimestampDesc(userId);
        Collections.reverse(recent);
        List<Map<String, String>> history = new ArrayList<>();
        for (ChatHistory c : recent) {
            Map<String, String> u = new HashMap<>();
            u.put("role", "user"); u.put("content", c.getMessage());
            history.add(u);
            if (c.getResponse() != null && !c.getResponse().isBlank()) {
                Map<String, String> a = new HashMap<>();
                a.put("role", "assistant"); a.put("content", c.getResponse());
                history.add(a);
            }
        }
        return history;
    }

    public List<ChatHistory> getHistory(Long userId) {
        return chatRepo.findTop50ByUserIdOrderByTimestampDesc(userId);
    }

    public void clearHistory(Long userId) {
        chatRepo.deleteAll(chatRepo.findByUserIdOrderByTimestampDesc(userId));
    }
}
