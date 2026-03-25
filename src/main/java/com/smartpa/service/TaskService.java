package com.smartpa.service;

import com.smartpa.dto.DTOs.*;
import com.smartpa.model.Task;
import com.smartpa.model.Task.TaskStatus;
import com.smartpa.model.Task.Priority;
import com.smartpa.model.User;
import com.smartpa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final ChatHistoryRepository chatRepo;
    private final NoteRepository noteRepo;

    public TaskResponse createTask(TaskRequest req, Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return toRes(taskRepo.save(Task.builder()
                .title(req.getTitle()).description(req.getDescription())
                .reminderTime(req.getReminderTime())
                .status(TaskStatus.PENDING).priority(parsePri(req.getPriority()))
                .user(user).build()));
    }

    public List<TaskResponse> getTasks(Long userId) {
        return taskRepo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toRes).collect(Collectors.toList());
    }

    public TaskResponse updateTask(Long id, TaskRequest req, Long userId) {
        Task t = owned(id, userId);
        t.setTitle(req.getTitle()); t.setDescription(req.getDescription());
        t.setReminderTime(req.getReminderTime());
        if (req.getPriority() != null) t.setPriority(parsePri(req.getPriority()));
        return toRes(taskRepo.save(t));
    }

    public TaskResponse completeTask(Long id, String note, Long userId) {
        Task t = owned(id, userId);
        t.setStatus(TaskStatus.COMPLETED);
        t.setCompletedAt(LocalDateTime.now());
        if (note != null && !note.isBlank()) t.setCompletionNote(note);
        return toRes(taskRepo.save(t));
    }

    public TaskResponse reopenTask(Long id, Long userId) {
        Task t = owned(id, userId);
        t.setStatus(TaskStatus.PENDING); t.setCompletedAt(null); t.setCompletionNote(null);
        return toRes(taskRepo.save(t));
    }

    public void cancelTask(Long id, Long userId) {
        Task t = owned(id, userId); t.setStatus(TaskStatus.CANCELLED); taskRepo.save(t);
    }

    public void deleteTask(Long id, Long userId) { taskRepo.delete(owned(id, userId)); }

    public StatsResponse getStats(Long userId) {
        List<Task> all = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);
        long total     = all.size();
        long pending   = all.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long completed = all.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long cancelled = all.stream().filter(t -> t.getStatus() == TaskStatus.CANCELLED).count();
        long high      = all.stream().filter(t -> t.getPriority() == Priority.HIGH && t.getStatus() == TaskStatus.PENDING).count();
        double rate    = total > 0 ? Math.round((completed * 100.0) / total) : 0;
        return StatsResponse.builder()
                .totalTasks(total).pendingTasks(pending).completedTasks(completed)
                .cancelledTasks(cancelled).highPriorityTasks(high)
                .totalChats(chatRepo.countByUserId(userId))
                .totalNotes(noteRepo.findByUserIdOrderByUpdatedAtDesc(userId).size())
                .completionRate(rate).build();
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now(), soon = now.plusMinutes(1);
        taskRepo.findByReminderTimeBetweenAndReminderSentFalseAndStatus(now, soon, TaskStatus.PENDING)
            .forEach(t -> { t.setReminderSent(true); taskRepo.save(t);
                log.info("REMINDER: '{}' for {}", t.getTitle(), t.getUser().getEmail()); });
    }

    private Task owned(Long id, Long userId) {
        Task t = taskRepo.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        if (!t.getUser().getId().equals(userId)) throw new RuntimeException("Unauthorized");
        return t;
    }
    private Priority parsePri(String p) {
        if (p == null) return Priority.MEDIUM;
        try { return Priority.valueOf(p.toUpperCase()); } catch (Exception e) { return Priority.MEDIUM; }
    }
    private TaskResponse toRes(Task t) {
        return TaskResponse.builder()
                .id(t.getId()).title(t.getTitle()).description(t.getDescription())
                .reminderTime(t.getReminderTime()).status(t.getStatus().name())
                .priority(t.getPriority() != null ? t.getPriority().name() : "MEDIUM")
                .completionNote(t.getCompletionNote()).completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt()).build();
    }
}
