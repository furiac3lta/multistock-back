package com.marcedev.stock.repository;

import com.marcedev.stock.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByNameIgnoreCase(String name);
}
