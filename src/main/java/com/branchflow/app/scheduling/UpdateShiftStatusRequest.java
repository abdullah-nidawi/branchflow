package com.branchflow.app.scheduling;

import jakarta.validation.constraints.NotNull;

public record UpdateShiftStatusRequest(@NotNull ShiftStatus status) {}
