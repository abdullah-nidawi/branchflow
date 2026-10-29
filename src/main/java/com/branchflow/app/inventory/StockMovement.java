package com.branchflow.app.inventory;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movement", indexes = @Index(name = "idx_movement_item_recorded", columnList = "stock_item_id, recorded_at, id"))
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 24)
    private MovementType type;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "quantity_after", nullable = false)
    private int quantityAfter;

    @Column(nullable = false, length = 300)
    private String reason;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "transfer_id")
    private UUID transferId;

    protected StockMovement() {}
    public StockMovement(StockItem stockItem, MovementType type, int quantityChange,
                         int quantityAfter, String reason, Instant recordedAt, UUID transferId) {
        this.stockItem = stockItem;
        this.type = type;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
        this.recordedAt = recordedAt;
        this.transferId = transferId;
    }
    public Long getId() { return id; }
    public StockItem getStockItem() { return stockItem; }
    public MovementType getType() { return type; }
    public int getQuantityChange() { return quantityChange; }
    public int getQuantityAfter() { return quantityAfter; }
    public String getReason() { return reason; }
    public Instant getRecordedAt() { return recordedAt; }
    public UUID getTransferId() { return transferId; }
}
