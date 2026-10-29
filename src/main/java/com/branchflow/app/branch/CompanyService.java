package com.branchflow.app.branch;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CompanyService {
    private final CompanyRepository companies;
    public CompanyService(CompanyRepository companies) { this.companies = companies; }

    @Transactional
    public CompanyResponse create(CreateCompanyRequest request) {
        return CompanyResponse.from(companies.save(new Company(request.name().trim())));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companies.findAll().stream().map(CompanyResponse::from).toList();
    }
}
