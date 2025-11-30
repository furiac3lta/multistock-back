package com.marcedev.stock.repository;

import com.marcedev.stock.entity.LogMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogMovementRepository extends JpaRepository<LogMovement, Long> {

    List<LogMovement> findAllByOrderByCreatedAtDesc();

    List<LogMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<LogMovement> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    List<LogMovement> findByUsernameOrderByCreatedAtDesc(String username);
}
