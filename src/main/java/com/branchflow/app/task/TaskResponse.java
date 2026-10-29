package com.branchflow.app.task;

import java.time.Instant;

public record TaskResponse(Long id, Long branchId, String title, String description,
                           TaskPriority priority, TaskStatus status, Instant dueAt, Instant createdAt) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getBranch().getId(), task.getTitle(), task.getDescription(),
                task.getPriority(), task.getStatus(), task.getDueAt(), task.getCreatedAt());
    }
}
