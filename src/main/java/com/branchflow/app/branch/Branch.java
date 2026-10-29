package com.branchflow.app.branch;

import jakarta.persistence.*;

@Entity
@Table(name = "branch", indexes = @Index(name = "idx_branch_company", columnList = "company_id"))
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    protected Branch() {}

    public Branch(Company company, String name, String city) {
        this.company = company;
        this.name = name;
        this.city = city;
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public String getName() { return name; }
    public String getCity() { return city; }
}
