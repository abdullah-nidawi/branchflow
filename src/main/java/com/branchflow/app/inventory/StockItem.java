package com.branchflow.app.inventory;

import com.branchflow.app.branch.Branch;
import com.branchflow.app.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_item", uniqueConstraints =
        @UniqueConstraint(name = "uq_stock_branch_product", columnNames = {"branch_id", "product_id"}))
public class StockItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "minimum_quantity", nullable = false)
    private int minimumQuantity;

    @Version
    @Column(nullable = false)
    private Long version;

    protected StockItem() {}
    public StockItem(Branch branch, Product product, int minimumQuantity) {
        this.branch = branch;
        this.product = product;
        this.minimumQuantity = minimumQuantity;
    }
    public void apply(int change) { this.quantity = Math.addExact(quantity, change); }
    public Long getId() { return id; }
    public Branch getBranch() { return branch; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public int getMinimumQuantity() { return minimumQuantity; }
}
