package com.marcedev.stock.dto;

import lombok.Data;

@Data
public class ImportProductDTO {
    private String name;
    private String sku;
    private Integer stock;
    private Double cost;
    private Double price;
    private String category;
    private String branch;
}
