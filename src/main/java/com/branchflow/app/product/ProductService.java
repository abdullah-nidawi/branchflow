package com.branchflow.app.product;

import com.branchflow.app.branch.Company;
import com.branchflow.app.branch.CompanyRepository;
import com.branchflow.app.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {
    private final ProductRepository products;
    private final CompanyRepository companies;
    public ProductService(ProductRepository products, CompanyRepository companies) {
        this.products = products;
        this.companies = companies;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Company company = companies.findById(request.companyId())
                .orElseThrow(() -> new NotFoundException("Company not found: " + request.companyId()));
        String sku = request.sku().trim().toUpperCase(Locale.ROOT);
        return ProductResponse.from(products.save(new Product(company, sku, request.name().trim())));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(Long companyId) {
        if (!companies.existsById(companyId)) throw new NotFoundException("Company not found: " + companyId);
        return products.findAllByCompany_IdOrderByNameAsc(companyId).stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return products.findById(id).map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }
}
