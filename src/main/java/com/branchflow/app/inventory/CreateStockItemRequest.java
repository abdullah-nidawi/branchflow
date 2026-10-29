package com.branchflow.app.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateStockItemRequest(@NotNull Long productId, @Min(0) int minimumQuantity) {}
