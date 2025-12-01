package com.marcedev.stock.dto.dashboard;

public record CategoryStockDTO(
        String category,
        Integer totalStock
) {}