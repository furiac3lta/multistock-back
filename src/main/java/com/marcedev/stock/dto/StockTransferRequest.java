package com.marcedev.stock.dto;

import lombok.Data;

@Data
public class StockTransferRequest {

    private Long sourceBranchId;
    private Long targetBranchId;

    private Long productId;
    private Integer quantity;

    private String description;
    private String user;
    private String ip;

}
