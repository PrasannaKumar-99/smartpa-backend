package com.smartpa.service;

import com.smartpa.dto.DTOs.*;
import com.smartpa.model.Task;
import com.smartpa.model.Task.TaskStatus;
import com.smartpa.model.Task.Priority;
import com.smartpa.repository.*;
import com.smartpa.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final TaskRepository taskRepo;
    private final ChatHistoryRepository chatRepo;
    private final NoteRepository noteRepo;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        long totalTasks = taskRepo.countByUserId(userId);
        long completed = taskRepo.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        return ProfileResponse.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .totalTasks(totalTasks).completedTasks(completed)
                .totalChats(chatRepo.countByUserId(userId))
                .totalNotes(noteRepo.countByUserId(userId))
                .build();
    }

    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepo.findById(userId).orElseThrow();
        if (req.getName() != null && !req.getName().isBlank()) user.setName(req.getName());
        userRepo.save(user);
        return getProfile(userId);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userRepo.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (req.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }

    @Cacheable(value = "userActivity", key = "#userId")
    public ActivityResponse getActivity(Long userId) {
        // Use DB aggregation instead of loading all tasks into memory
        LocalDateTime since = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> createdCounts = taskRepo.countByCreatedAtGrouped(userId, since);
        Map<LocalDate, Long> createdMap = new HashMap<>();
        for (Object[] row : createdCounts) {
            createdMap.put((LocalDate) row[0], (Long) row[1]);
        }

        // Last 7 days tasks created
        List<Map<String, Object>> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = createdMap.getOrDefault(day, 0L);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("day", day.getDayOfWeek().toString().substring(0, 3));
            entry.put("date", day.toString());
            entry.put("count", count);
            weekly.add(entry);
        }

        // By priority (from DB aggregation)
        List<Object[]> priorityCounts = taskRepo.countByPriorityGrouped(userId);
        Map<Priority, Long> priorityMap = new EnumMap<>(Priority.class);
        for (Object[] row : priorityCounts) {
            priorityMap.put((Priority) row[0], (Long) row[1]);
        }
        List<Map<String, Object>> byPriority = new ArrayList<>();
        for (Priority p : Priority.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("priority", p.name()); m.put("count", priorityMap.getOrDefault(p, 0L));
            byPriority.add(m);
        }

        // By status (from DB aggregation)
        List<Object[]> statusCounts = taskRepo.countByStatusGrouped(userId);
        Map<TaskStatus, Long> statusMap = new EnumMap<>(TaskStatus.class);
        for (Object[] row : statusCounts) {
            statusMap.put((TaskStatus) row[0], (Long) row[1]);
        }
        List<Map<String, Object>> byStatus = new ArrayList<>();
        for (TaskStatus s : TaskStatus.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", s.name()); m.put("count", statusMap.getOrDefault(s, 0L));
            byStatus.add(m);
        }

        return ActivityResponse.builder()
                .weeklyTasksCreated(weekly)
                .tasksByPriority(byPriority)
                .tasksByStatus(byStatus)
                .streakDays(calculateStreakFromDb(userId))
                .build();
    }

    private long calculateStreakFromDb(Long userId) {
        long streak = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            long count = taskRepo.countByUserIdAndCreatedAtBetween(userId, start, end);
            if (count > 0) streak++;
            else if (i > 0) break;
        }
        return streak;
    }
}
