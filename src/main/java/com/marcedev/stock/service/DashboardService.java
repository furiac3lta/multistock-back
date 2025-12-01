package com.marcedev.stock.service;

import com.marcedev.stock.dto.dashboard.BranchProductCountDTO;
import com.marcedev.stock.dto.dashboard.CategoryStockDTO;
import com.marcedev.stock.dto.dashboard.Movements30DTO;

import java.util.List;

public interface DashboardService {

    List<CategoryStockDTO> getStockByCategory(Long branchId);

    List<BranchProductCountDTO> getProductsByBranch();

    List<Movements30DTO> getMovementsLast30Days(Long branchId);
}
