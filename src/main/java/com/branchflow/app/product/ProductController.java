package com.branchflow.app.product;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) { return service.create(request); }

    @GetMapping
    public List<ProductResponse> findAll(@RequestParam Long companyId) { return service.findAll(companyId); }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id) { return service.findById(id); }
}
