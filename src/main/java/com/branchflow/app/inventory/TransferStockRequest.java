package com.branchflow.app.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferStockRequest(
        @NotNull Long productId,
        @NotNull Long fromBranchId,
        @NotNull Long toBranchId,
        @Positive int quantity,
        @NotBlank @Size(max = 300) String reason) {}
