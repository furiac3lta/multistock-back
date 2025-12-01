package com.marcedev.stock.service.impl;

import com.marcedev.stock.dto.dashboard.BranchProductCountDTO;
import com.marcedev.stock.dto.dashboard.CategoryStockDTO;
import com.marcedev.stock.dto.dashboard.Movements30DTO;
import com.marcedev.stock.entity.Product;
import com.marcedev.stock.repository.BranchRepository;
import com.marcedev.stock.repository.ProductRepository;
import com.marcedev.stock.repository.StockMovementRepository;
import com.marcedev.stock.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepo;
    private final BranchRepository branchRepo;
    private final StockMovementRepository movementRepo;

    @Override
    public List<CategoryStockDTO> getStockByCategory(Long branchId) {

        return productRepo.findByBranchId(branchId).stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.summingInt(Product::getStock)
                ))
                .entrySet()
                .stream()
                .map(e -> new CategoryStockDTO(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<BranchProductCountDTO> getProductsByBranch() {

        return branchRepo.findAll().stream()
                .map(branch -> {
                    int count = productRepo.countByBranchId(branch.getId());
                    return new BranchProductCountDTO(branch.getName(), count);
                })
                .toList();
    }

    @Override
    public List<Movements30DTO> getMovementsLast30Days(Long branchId) {

        LocalDate start = LocalDate.now().minusDays(30);

        return movementRepo.findByBranchIdAndCreatedAtAfter(branchId, start.atStartOfDay())
                .stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new Movements30DTO(e.getKey().toString(), e.getValue()))
                .toList();
    }
}
