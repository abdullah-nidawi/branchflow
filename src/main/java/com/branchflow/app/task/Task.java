package com.branchflow.app.task;

import com.branchflow.app.branch.Branch;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task", indexes = @Index(name = "idx_task_branch_status", columnList = "branch_id, status"))
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(columnDefinition = "text")
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private TaskPriority priority;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TaskStatus status;
    @Column(name = "due_at")
    private Instant dueAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Task() {}
    public Task(Branch branch, String title, String description, TaskPriority priority, Instant dueAt) {
        this.branch = branch;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TaskStatus.OPEN;
        this.dueAt = dueAt;
        this.createdAt = Instant.now();
    }
    public void changeStatus(TaskStatus status) { this.status = status; }
    public Long getId() { return id; }
    public Branch getBranch() { return branch; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public Instant getDueAt() { return dueAt; }
    public Instant getCreatedAt() { return createdAt; }
}
