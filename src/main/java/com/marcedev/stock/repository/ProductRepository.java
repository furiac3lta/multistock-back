package com.marcedev.stock.repository;

import com.marcedev.stock.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBranchId(Long branchId);
    int countByBranchId(Long branchId);

    // Buscar producto por SKU y sucursal — DEVUELVE Optional<Product>
    @Query("SELECT p FROM Product p WHERE UPPER(p.sku) = UPPER(:sku) AND p.branch.id = :branchId")
    Optional<Product> findBySkuAndBranch(
            @Param("sku") String sku,
            @Param("branchId") Long branchId
    );
}
