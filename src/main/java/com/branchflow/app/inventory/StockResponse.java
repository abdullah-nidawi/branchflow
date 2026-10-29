package com.branchflow.app.inventory;

public record StockResponse(Long id, Long branchId, Long productId, String sku, String productName,
                            int quantity, int minimumQuantity, boolean lowStock) {
    public static StockResponse from(StockItem item) {
        return new StockResponse(item.getId(), item.getBranch().getId(), item.getProduct().getId(),
                item.getProduct().getSku(), item.getProduct().getName(), item.getQuantity(),
                item.getMinimumQuantity(), item.getQuantity() <= item.getMinimumQuantity());
    }
}
