package com.branchflow.app.branch;

public record CompanyResponse(Long id, String name) {
    public static CompanyResponse from(Company company) { return new CompanyResponse(company.getId(), company.getName()); }
}
