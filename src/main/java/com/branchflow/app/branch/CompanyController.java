package com.branchflow.app.branch;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;
    public CompanyController(CompanyService companyService) { this.companyService = companyService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CreateCompanyRequest request) { return companyService.create(request); }

    @GetMapping
    public List<CompanyResponse> findAll() { return companyService.findAll(); }
}
