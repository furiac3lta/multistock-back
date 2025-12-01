package com.marcedev.stock.service;

import com.marcedev.stock.dto.ImportProductDTO;
import com.marcedev.stock.dto.ProductDto;
import com.marcedev.stock.entity.Product;

import java.util.List;

public interface ProductService {

    List<ProductDto> findAll();

    List<ProductDto> findByBranch(Long branchId);


    ProductDto findById(Long id);

    ProductDto create(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);

    void delete(Long id);

    void importProducts(List<ImportProductDTO> list);

}
