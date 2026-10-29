package com.branchflow.app.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class InventoryController {
    private final InventoryService inventory;
    public InventoryController(InventoryService inventory) { this.inventory = inventory; }

    @PostMapping("/api/branches/{branchId}/stock")
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponse createStockItem(@PathVariable Long branchId, @Valid @RequestBody CreateStockItemRequest request) {
        return inventory.createStockItem(branchId, request);
    }

    @GetMapping("/api/branches/{branchId}/stock")
    public List<StockResponse> stock(@PathVariable Long branchId) { return inventory.findStock(branchId, false); }

    @GetMapping("/api/branches/{branchId}/stock/low")
    public List<StockResponse> lowStock(@PathVariable Long branchId) { return inventory.findStock(branchId, true); }

    @PostMapping("/api/stock/{stockItemId}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse recordMovement(@PathVariable Long stockItemId, @Valid @RequestBody CreateMovementRequest request) {
        return inventory.recordMovement(stockItemId, request);
    }

    @GetMapping("/api/stock/{stockItemId}/movements")
    public List<MovementResponse> history(@PathVariable Long stockItemId) { return inventory.findMovements(stockItemId); }

    @PostMapping("/api/stock/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MovementResponse> transfer(@Valid @RequestBody TransferStockRequest request) { return inventory.transfer(request); }
}
