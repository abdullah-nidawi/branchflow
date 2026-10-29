package com.branchflow.app.product;

public record ProductResponse(Long id, Long companyId, String sku, String name, boolean active) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getCompany().getId(), product.getSku(), product.getName(), product.isActive());
    }
}
