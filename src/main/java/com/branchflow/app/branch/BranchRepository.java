package com.branchflow.app.branch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findAllByCompany_IdOrderByNameAsc(Long companyId);
}
