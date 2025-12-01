package com.marcedev.stock.controller;

import com.marcedev.stock.dto.ImportProductDTO;
import com.marcedev.stock.dto.ProductDto;
import com.marcedev.stock.dto.StockMovementDto;
import com.marcedev.stock.service.ProductService;
import com.marcedev.stock.service.StockMovementService;
import com.marcedev.stock.service.impl.StockMovementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // ---------------------------------------------------------
    // GET ALL (sin filtrar sucursal)
    // ---------------------------------------------------------
    @GetMapping
    public List<ProductDto> findAll() {
        return service.findAll();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // ---------------------------------------------------------
    // GET POR SUCURSAL
    // /products/branch/3
    // ---------------------------------------------------------
    @GetMapping("/branch/{branchId}")
    public List<ProductDto> findByBranch(@PathVariable Long branchId) {
        return service.findByBranch(branchId);
    }

    // ---------------------------------------------------------
    // CREATE PRODUCT
    // ---------------------------------------------------------
    @PostMapping
    public ProductDto create(@RequestBody ProductDto dto) {

        // Validación: nombre no vacío
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
        }

        // Validación: SKU no vacío
        if (dto.getSku() == null || dto.getSku().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El SKU no puede estar vacío");
        }

        // Validación: stock inicial >= 0
        if (dto.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock inicial no puede ser negativo");
        }

        // Validación: precio costo >= 0
        if (dto.getCostPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de costo no puede ser negativo");
        }

        // Validación: precio venta >= 0
        if (dto.getSalePrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de venta no puede ser negativo");
        }

        // Validación: categoría requerida
        if (dto.getCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una categoría");
        }

        // Validación: sucursal requerida
        if (dto.getBranchId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una sucursal");
        }

        return service.create(dto);
    }

    // ---------------------------------------------------------
    // UPDATE PRODUCT
    // ---------------------------------------------------------
    @PutMapping("/{id}")
    public ProductDto update(@PathVariable Long id, @RequestBody ProductDto dto) {

        // Evitamos valores negativos
        if (dto.getStock() != null && dto.getStock() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo");

        if (dto.getCostPrice() != null && dto.getCostPrice() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de costo no puede ser negativo");

        if (dto.getSalePrice() != null && dto.getSalePrice() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de venta no puede ser negativo");

        // Validación: categoría no nula
        if (dto.getCategoryId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una categoría");

        // Validación: sucursal no nula
        if (dto.getBranchId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar una sucursal");

        return service.update(id, dto);
    }

    // ---------------------------------------------------------
    // DELETE PRODUCT (soft delete)
    // ---------------------------------------------------------
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }


    @PostMapping("/import")
    public ResponseEntity<?> importProducts(@RequestBody List<ImportProductDTO> list) {
        service.importProducts(list);
        return ResponseEntity.ok("IMPORT_OK");
    }

}
