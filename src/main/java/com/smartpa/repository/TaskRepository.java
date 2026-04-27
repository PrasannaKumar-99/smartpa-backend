package com.smartpa.repository;

import com.smartpa.model.Task;
import com.smartpa.model.Task.TaskStatus;
import com.smartpa.model.Task.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Task> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    List<Task> findByUserIdAndPriority(Long userId, Priority priority);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    long countByUserIdAndPriority(Long userId, Priority priority);
    long countByUserIdAndPriorityAndStatus(Long userId, Priority priority, TaskStatus status);
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<Task> findByReminderTimeBetweenAndReminderSentFalseAndStatus(
        LocalDateTime start, LocalDateTime end, TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.user.id = :userId GROUP BY t.status")
    List<Object[]> countByStatusGrouped(@Param("userId") Long userId);

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.user.id = :userId GROUP BY t.priority")
    List<Object[]> countByPriorityGrouped(@Param("userId") Long userId);

    @Query("SELECT CAST(t.createdAt AS LocalDate), COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.createdAt >= :since GROUP BY CAST(t.createdAt AS LocalDate)")
    List<Object[]> countByCreatedAtGrouped(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
