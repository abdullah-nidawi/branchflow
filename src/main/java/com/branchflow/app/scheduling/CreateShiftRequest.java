package com.branchflow.app.scheduling;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateShiftRequest(@NotNull Long employeeId, @NotNull Instant startTime, @NotNull Instant endTime) {}
