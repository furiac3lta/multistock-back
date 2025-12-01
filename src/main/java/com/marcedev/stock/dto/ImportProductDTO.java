package com.marcedev.stock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor   // ← NECESARIO PARA DESERIALIZAR
@AllArgsConstructor
public class ImportProductDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("stock")
    private Integer stock;

    @JsonProperty("cost")
    private Double cost;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("category")
    private String category;

    @JsonProperty("branch")
    private String branch;

    @Override
    public String toString() {
        return "DTO{name='%s', sku='%s', stock=%s, cost=%s, price=%s, category='%s', branch='%s'}"
                .formatted(name, sku, stock, cost, price, category, branch);
    }
}
