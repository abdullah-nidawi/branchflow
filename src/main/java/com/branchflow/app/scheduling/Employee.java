package com.branchflow.app.scheduling;

import com.branchflow.app.branch.Branch;
import jakarta.persistence.*;

@Entity
@Table(name = "employee", uniqueConstraints =
        @UniqueConstraint(name = "uq_employee_branch_email", columnNames = {"branch_id", "email"}))
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 254)
    private String email;

    protected Employee() {}
    public Employee(Branch branch, String name, String email) {
        this.branch = branch;
        this.name = name;
        this.email = email;
    }
    public Long getId() { return id; }
    public Branch getBranch() { return branch; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
