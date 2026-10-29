package com.branchflow.app.scheduling;

import java.time.Instant;

public record ShiftResponse(Long id, Long branchId, Long employeeId, String employeeName,
                            Instant startTime, Instant endTime, ShiftStatus status) {
    public static ShiftResponse from(Shift shift) {
        return new ShiftResponse(shift.getId(), shift.getBranch().getId(), shift.getEmployee().getId(),
                shift.getEmployee().getName(), shift.getStartTime(), shift.getEndTime(), shift.getStatus());
    }
}
