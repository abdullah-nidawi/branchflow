package com.branchflow.app.scheduling;

public record EmployeeResponse(Long id, Long branchId, String name, String email) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getId(), employee.getBranch().getId(), employee.getName(), employee.getEmail());
    }
}
