package com.branchflow.app.scheduling;

import com.branchflow.app.branch.Branch;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shift", indexes = @Index(name = "idx_shift_employee_time", columnList = "employee_id, start_time, end_time"))
public class Shift {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private ShiftStatus status;

    protected Shift() {}
    public Shift(Branch branch, Employee employee, Instant startTime, Instant endTime) {
        this.branch = branch;
        this.employee = employee;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ShiftStatus.PLANNED;
    }
    public void changeStatus(ShiftStatus status) { this.status = status; }
    public Long getId() { return id; }
    public Branch getBranch() { return branch; }
    public Employee getEmployee() { return employee; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public ShiftStatus getStatus() { return status; }
}
