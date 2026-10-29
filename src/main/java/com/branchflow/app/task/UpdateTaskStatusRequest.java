package com.branchflow.app.task;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(@NotNull TaskStatus status) {}
