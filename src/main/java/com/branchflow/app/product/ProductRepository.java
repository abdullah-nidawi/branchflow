package com.branchflow.app.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByCompany_IdOrderByNameAsc(Long companyId);
}
