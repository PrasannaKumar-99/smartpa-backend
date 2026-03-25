package com.smartpa.service;

import com.smartpa.dto.DTOs.*;
import com.smartpa.model.Task;
import com.smartpa.repository.*;
import com.smartpa.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        List<Task> tasks = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);
        long completed = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        return ProfileResponse.builder()
                .id(user.getId()).name(user.getName()).email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .totalTasks(tasks.size()).completedTasks(completed)
                .totalChats(chatRepo.countByUserId(userId))
                .totalNotes(noteRepo.findByUserIdOrderByUpdatedAtDesc(userId).size())
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

    public ActivityResponse getActivity(Long userId) {
        List<Task> tasks = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);

        // Last 7 days tasks created
        List<Map<String, Object>> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = tasks.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(day))
                .count();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("day", day.getDayOfWeek().toString().substring(0, 3));
            entry.put("date", day.toString());
            entry.put("count", count);
            weekly.add(entry);
        }

        // By priority
        List<Map<String, Object>> byPriority = new ArrayList<>();
        for (Task.Priority p : Task.Priority.values()) {
            long cnt = tasks.stream().filter(t -> t.getPriority() == p).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("priority", p.name()); m.put("count", cnt);
            byPriority.add(m);
        }

        // By status
        List<Map<String, Object>> byStatus = new ArrayList<>();
        for (Task.TaskStatus s : Task.TaskStatus.values()) {
            long cnt = tasks.stream().filter(t -> t.getStatus() == s).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", s.name()); m.put("count", cnt);
            byStatus.add(m);
        }

        return ActivityResponse.builder()
                .weeklyTasksCreated(weekly)
                .tasksByPriority(byPriority)
                .tasksByStatus(byStatus)
                .streakDays(calculateStreak(tasks))
                .build();
    }

    private long calculateStreak(List<Task> tasks) {
        long streak = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate day = today.minusDays(i);
            boolean hasActivity = tasks.stream()
                .anyMatch(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(day));
            if (hasActivity) streak++;
            else if (i > 0) break;
        }
        return streak;
    }
}
