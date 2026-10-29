package com.branchflow.app.scheduling;

import com.branchflow.app.branch.Branch;
import com.branchflow.app.branch.BranchRepository;
import com.branchflow.app.common.ConflictException;
import com.branchflow.app.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class SchedulingService {
    private final EmployeeRepository employees;
    private final ShiftRepository shifts;
    private final BranchRepository branches;

    public SchedulingService(EmployeeRepository employees, ShiftRepository shifts, BranchRepository branches) {
        this.employees = employees;
        this.shifts = shifts;
        this.branches = branches;
    }

    @Transactional
    public EmployeeResponse createEmployee(Long branchId, CreateEmployeeRequest request) {
        Branch branch = branches.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (employees.existsByBranch_IdAndEmail(branchId, email))
            throw new ConflictException("An employee with this email already exists at this branch");
        return EmployeeResponse.from(employees.save(new Employee(branch, request.name().trim(), email)));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findEmployees(Long branchId) {
        requireBranch(branchId);
        return employees.findAllByBranch_IdOrderByNameAsc(branchId).stream().map(EmployeeResponse::from).toList();
    }

    @Transactional
    public ShiftResponse createShift(Long branchId, CreateShiftRequest request) {
        Branch branch = branches.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        // Locking the employee serializes schedule writes for that person, preventing two requests
        // from both passing the overlap check before either shift is committed.
        Employee employee = employees.findLockedById(request.employeeId())
                .orElseThrow(() -> new NotFoundException("Employee not found: " + request.employeeId()));
        if (!employee.getBranch().getId().equals(branchId))
            throw new ConflictException("Employee must belong to the shift's branch");
        if (!request.endTime().isAfter(request.startTime()))
            throw new ConflictException("Shift end time must be after its start time");
        if (shifts.hasOverlap(employee.getId(), request.startTime(), request.endTime(), ShiftStatus.CANCELLED, null))
            throw new ConflictException("This shift overlaps another active shift for the employee");
        return ShiftResponse.from(shifts.save(new Shift(branch, employee, request.startTime(), request.endTime())));
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> findShifts(Long branchId) {
        requireBranch(branchId);
        return shifts.findAllByBranch_IdOrderByStartTimeAsc(branchId).stream().map(ShiftResponse::from).toList();
    }

    @Transactional
    public ShiftResponse updateShiftStatus(Long shiftId, ShiftStatus status) {
        Shift shift = shifts.findLockedById(shiftId).orElseThrow(() -> new NotFoundException("Shift not found: " + shiftId));
        if (status != ShiftStatus.CANCELLED) {
            Employee employee = employees.findLockedById(shift.getEmployee().getId())
                    .orElseThrow(() -> new NotFoundException("Employee not found: " + shift.getEmployee().getId()));
            if (shifts.hasOverlap(employee.getId(), shift.getStartTime(), shift.getEndTime(), ShiftStatus.CANCELLED, shift.getId()))
                throw new ConflictException("This shift overlaps another active shift for the employee");
        }
        shift.changeStatus(status);
        return ShiftResponse.from(shift);
    }

    private void requireBranch(Long branchId) {
        if (!branches.existsById(branchId)) throw new NotFoundException("Branch not found: " + branchId);
    }
}
