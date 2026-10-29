package com.branchflow.app.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select t from Task t where t.branch.id = :branchId " +
            "and (:status is null or t.status = :status) " +
            "and (:priority is null or t.priority = :priority)")
    Page<Task> search(@Param("branchId") Long branchId, @Param("status") TaskStatus status,
                      @Param("priority") TaskPriority priority, Pageable pageable);
}
