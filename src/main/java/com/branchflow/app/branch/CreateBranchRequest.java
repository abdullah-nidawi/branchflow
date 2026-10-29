package com.branchflow.app.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotNull Long companyId,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String city) {}
