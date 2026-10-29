package com.branchflow.app.inventory;

import java.time.Instant;
import java.util.UUID;

public record MovementResponse(Long id, Long stockItemId, MovementType type, int quantityChange,
                               int quantityAfter, String reason, Instant recordedAt, UUID transferId) {
    public static MovementResponse from(StockMovement movement) {
        return new MovementResponse(movement.getId(), movement.getStockItem().getId(), movement.getType(),
                movement.getQuantityChange(), movement.getQuantityAfter(), movement.getReason(),
                movement.getRecordedAt(), movement.getTransferId());
    }
}
