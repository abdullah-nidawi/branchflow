package com.branchflow.app.scheduling;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email) {}
