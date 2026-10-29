package com.branchflow.app.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findAllByStockItem_IdOrderByRecordedAtDescIdDesc(Long stockItemId);
}
