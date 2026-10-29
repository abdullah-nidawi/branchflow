package com.branchflow.app.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    boolean existsByBranch_IdAndProduct_Id(Long branchId, Long productId);
    Optional<StockItem> findByBranch_IdAndProduct_Id(Long branchId, Long productId);
    List<StockItem> findAllByBranch_IdOrderByProduct_NameAsc(Long branchId);

    @Query("select s from StockItem s where s.branch.id = :branchId and s.quantity <= s.minimumQuantity order by s.product.name")
    List<StockItem> findLowStockByBranch(@Param("branchId") Long branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.id = :id")
    Optional<StockItem> findLockedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.id in :ids order by s.id")
    List<StockItem> findAllLockedByIds(@Param("ids") Collection<Long> ids);
}
