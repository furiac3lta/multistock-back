package com.marcedev.stock.repository;

import com.marcedev.stock.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT m FROM StockMovement m WHERE m.product.branch.id = :branchId ORDER BY m.createdAt DESC")
    List<StockMovement> historyByBranch(@Param("branchId") Long branchId);

    List<StockMovement> findByProductBranchIdOrderByCreatedAtDesc(Long branchId);

    List<StockMovement> findByBranchIdAndCreatedAtAfter(Long branchId, LocalDateTime createdAt);

}
