package com.branchflow.app.branch;

import com.branchflow.app.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BranchService {
    private final BranchRepository branches;
    private final CompanyRepository companies;

    public BranchService(BranchRepository branches, CompanyRepository companies) {
        this.branches = branches;
        this.companies = companies;
    }

    @Transactional
    public BranchResponse create(CreateBranchRequest request) {
        Company company = companies.findById(request.companyId())
                .orElseThrow(() -> new NotFoundException("Company not found: " + request.companyId()));
        return BranchResponse.from(branches.save(new Branch(company, request.name().trim(), request.city().trim())));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> findAll(Long companyId) {
        if (companyId == null) return branches.findAll().stream().map(BranchResponse::from).toList();
        if (!companies.existsById(companyId)) throw new NotFoundException("Company not found: " + companyId);
        return branches.findAllByCompany_IdOrderByNameAsc(companyId).stream().map(BranchResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse findById(Long id) {
        return branches.findById(id).map(BranchResponse::from)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + id));
    }
}
