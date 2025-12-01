package com.marcedev.stock.controller;

import com.marcedev.stock.dto.dashboard.BranchProductCountDTO;
import com.marcedev.stock.dto.dashboard.CategoryStockDTO;
import com.marcedev.stock.dto.dashboard.Movements30DTO;
import com.marcedev.stock.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/stock-category/{branchId}")
    public List<CategoryStockDTO> stockByCategory(@PathVariable Long branchId) {
        return service.getStockByCategory(branchId);
    }

    @GetMapping("/stock-branch")
    public List<BranchProductCountDTO> stockByBranch() {
        return service.getProductsByBranch();
    }

    @GetMapping("/movements-30/{branchId}")
    public List<Movements30DTO> movements30(@PathVariable Long branchId) {
        return service.getMovementsLast30Days(branchId);
    }
}
