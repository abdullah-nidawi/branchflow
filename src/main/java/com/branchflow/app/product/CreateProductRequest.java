package com.branchflow.app.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotNull Long companyId,
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 150) String name) {}
