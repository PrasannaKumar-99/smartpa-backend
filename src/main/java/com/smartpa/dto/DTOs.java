package com.smartpa.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DTOs {

    @Data public static class RegisterRequest        { private String name, email, password; }
    @Data public static class LoginRequest           { private String email, password; }
    @Data public static class ChatRequest            { private String message; }
    @Data public static class NoteRequest            { private String title, content; }
    @Data public static class UpdateProfileRequest   { private String name; }
    @Data public static class ChangePasswordRequest  { private String currentPassword, newPassword; }
    @Data public static class SummarizeRequest       { private String text; }

    @Data public static class TaskRequest {
        private String title, description, priority, completionNote;
        private LocalDateTime reminderTime;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse { private String token, name, email; private Long userId; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChatResponse {
        private String message, response, intent, taskTitle;
        private LocalDateTime timestamp;
        private boolean taskCreated;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TaskResponse {
        private Long id;
        private String title, description, status, priority, completionNote;
        private LocalDateTime reminderTime, createdAt, completedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NoteResponse {
        private Long id;
        private String title, content;
        private LocalDateTime createdAt, updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StatsResponse {
        private long totalTasks, pendingTasks, completedTasks, cancelledTasks,
                     totalChats, highPriorityTasks, totalNotes;
        private double completionRate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private String name, email;
        private LocalDateTime createdAt;
        private long totalTasks, completedTasks, totalChats, totalNotes;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ActivityResponse {
        private List<Map<String, Object>> weeklyTasksCreated;
        private List<Map<String, Object>> tasksByPriority;
        private List<Map<String, Object>> tasksByStatus;
        private long streakDays;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SummarizeResponse { private String summary; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        public static <T> ApiResponse<T> success(String msg, T data) {
            return ApiResponse.<T>builder().success(true).message(msg).data(data).build();
        }
        public static <T> ApiResponse<T> error(String msg) {
            return ApiResponse.<T>builder().success(false).message(msg).build();
        }
    }
}
