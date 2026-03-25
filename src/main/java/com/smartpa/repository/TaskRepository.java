package com.smartpa.repository;

import com.smartpa.model.Task;
import com.smartpa.model.Task.TaskStatus;
import com.smartpa.model.Task.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    List<Task> findByUserIdAndPriority(Long userId, Priority priority);
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    long countByUserIdAndPriority(Long userId, Priority priority);
    List<Task> findByReminderTimeBetweenAndReminderSentFalseAndStatus(
        LocalDateTime start, LocalDateTime end, TaskStatus status);
}
