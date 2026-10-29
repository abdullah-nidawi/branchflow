package com.branchflow.app.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByBranch_IdOrderByNameAsc(Long branchId);
    boolean existsByBranch_IdAndEmail(Long branchId, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Employee e where e.id = :id")
    Optional<Employee> findLockedById(@Param("id") Long id);
}
