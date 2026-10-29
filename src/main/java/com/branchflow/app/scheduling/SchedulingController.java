package com.branchflow.app.scheduling;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class SchedulingController {
    private final SchedulingService service;
    public SchedulingController(SchedulingService service) { this.service = service; }

    @PostMapping("/api/branches/{branchId}/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse createEmployee(@PathVariable Long branchId, @Valid @RequestBody CreateEmployeeRequest request) {
        return service.createEmployee(branchId, request);
    }

    @GetMapping("/api/branches/{branchId}/employees")
    public List<EmployeeResponse> employees(@PathVariable Long branchId) { return service.findEmployees(branchId); }

    @PostMapping("/api/branches/{branchId}/shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse createShift(@PathVariable Long branchId, @Valid @RequestBody CreateShiftRequest request) {
        return service.createShift(branchId, request);
    }

    @GetMapping("/api/branches/{branchId}/shifts")
    public List<ShiftResponse> shifts(@PathVariable Long branchId) { return service.findShifts(branchId); }

    @PatchMapping("/api/shifts/{shiftId}/status")
    public ShiftResponse updateStatus(@PathVariable Long shiftId, @Valid @RequestBody UpdateShiftStatusRequest request) {
        return service.updateShiftStatus(shiftId, request.status());
    }
}
