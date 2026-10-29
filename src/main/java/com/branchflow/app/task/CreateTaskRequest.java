package com.branchflow.app.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateTaskRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 4000) String description,
        @NotNull TaskPriority priority,
        Instant dueAt) {}
