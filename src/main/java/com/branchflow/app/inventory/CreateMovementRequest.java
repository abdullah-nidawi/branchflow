package com.branchflow.app.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMovementRequest(
        @NotNull MovementType type,
        @NotNull Integer quantityChange,
        @NotBlank @Size(max = 300) String reason) {}
