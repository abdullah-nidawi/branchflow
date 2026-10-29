package com.branchflow.app.product;

import com.branchflow.app.branch.Company;
import jakarta.persistence.*;

@Entity
@Table(name = "product", uniqueConstraints =
        @UniqueConstraint(name = "uq_product_company_sku", columnNames = {"company_id", "sku"}))
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    protected Product() {}
    public Product(Company company, String sku, String name) {
        this.company = company;
        this.sku = sku;
        this.name = name;
    }
    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
