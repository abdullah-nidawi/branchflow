package com.branchflow.app.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findAllByBranch_IdOrderByStartTimeAsc(Long branchId);

    @Query("select case when count(s) > 0 then true else false end from Shift s " +
            "where s.employee.id = :employeeId and s.status <> :cancelled " +
            "and (:ignoredShiftId is null or s.id <> :ignoredShiftId) " +
            "and s.startTime < :newEnd and s.endTime > :newStart")
    boolean hasOverlap(@Param("employeeId") Long employeeId, @Param("newStart") Instant newStart,
                       @Param("newEnd") Instant newEnd, @Param("cancelled") ShiftStatus cancelled,
                       @Param("ignoredShiftId") Long ignoredShiftId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shift s where s.id = :id")
    Optional<Shift> findLockedById(@Param("id") Long id);
}
