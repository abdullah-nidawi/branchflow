package com.branchflow.app.branch;

import jakarta.persistence.*;

@Entity
@Table(name = "company")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    protected Company() {}

    public Company(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
}
