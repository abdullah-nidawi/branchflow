package com.branchflow.app.branch;

public record BranchResponse(Long id, Long companyId, String name, String city) {
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getCompany().getId(), branch.getName(), branch.getCity());
    }
}
