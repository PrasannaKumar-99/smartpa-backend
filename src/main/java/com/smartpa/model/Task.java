package com.smartpa.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_user_id", columnList = "user_id"),
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_priority", columnList = "priority"),
    @Index(name = "idx_task_created_at", columnList = "created_at"),
    @Index(name = "idx_task_reminder_time", columnList = "reminder_time")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Priority priority;

    @Column(name = "completion_note", columnDefinition = "TEXT")
    private String completionNote;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status   == null) status   = TaskStatus.PENDING;
        if (priority == null) priority = Priority.MEDIUM;
    }

    public enum TaskStatus { PENDING, COMPLETED, CANCELLED }
    public enum Priority   { HIGH, MEDIUM, LOW }
}
