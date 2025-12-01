package com.marcedev.stock.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMovementDto {

    private Long id;
    private LocalDateTime createdAt;
    private String username;

    private String movementType;
    private String productName;
    private Double quantity;
    private String description;

    private String branchName;
    private String originBranchName;

    private Double beforeStock;
    private Double afterStock;

    private String ip;
}
