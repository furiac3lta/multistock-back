package com.marcedev.stock.service.impl;

import com.marcedev.stock.dto.ImportProductDTO;
import com.marcedev.stock.dto.ProductDto;
import com.marcedev.stock.entity.Branch;
import com.marcedev.stock.entity.Category;
import com.marcedev.stock.entity.Product;
import com.marcedev.stock.mapper.ProductMapper;
import com.marcedev.stock.repository.BranchRepository;
import com.marcedev.stock.repository.CategoryRepository;
import com.marcedev.stock.repository.ProductRepository;
import com.marcedev.stock.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository; // ← SOLO UNO
    private final ProductMapper mapper;

    // ===========================================================
    // CRUD NORMAL
    // ===========================================================

    @Override
    public List<ProductDto> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ProductDto> findByBranch(Long branchId) {
        return repository.findByBranchId(branchId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public ProductDto findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @Override
    public ProductDto create(ProductDto dto) {

        validateDto(dto);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        Product product = mapper.toEntity(dto);
        product.setCategory(category);
        product.setBranch(branch);
        product.setActive(true);

        return mapper.toDto(repository.save(product));
    }

    @Override
    public ProductDto update(Long id, ProductDto dto) {

        validateDto(dto);

        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        mapper.updateFromDto(dto, product);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        product.setCategory(category);

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        product.setBranch(branch);

        return mapper.toDto(repository.save(product));
    }

    @Override
    public void delete(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setActive(false);
        repository.save(product);
    }

    // ===========================================================
    // IMPORTAR PRODUCTOS DESDE EXCEL
    // ===========================================================

    @Override
    @Transactional
    public void importProducts(List<ImportProductDTO> list) {
        System.out.println("== INICIO IMPORTACIÓN, FILAS RECIBIDAS: " + list.size());

        for (ImportProductDTO dto : list) {
            System.out.println("Procesando fila: " + dto);

            if (dto.getName() == null || dto.getCategory() == null || dto.getBranch() == null) {
                throw new RuntimeException("Faltan campos obligatorios en el Excel");
            }

            Category category = categoryRepository
                    .findByNameIgnoreCase(dto.getCategory())
                    .orElseThrow(() ->
                            new RuntimeException("Categoría no encontrada: " + dto.getCategory())
                    );

            Branch branch = branchRepository
                    .findByNameIgnoreCase(dto.getBranch())
                    .orElseThrow(() ->
                            new RuntimeException("Sucursal no encontrada: " + dto.getBranch())
                    );

            Product p = new Product();
            p.setName(dto.getName());
            p.setSku(dto.getSku());
            p.setStock(dto.getStock());
            p.setCostPrice(dto.getCost());
            p.setSalePrice(dto.getPrice());
            p.setCategory(category);
            p.setBranch(branch);
            p.setActive(true);

            repository.save(p); // ← CORREGIDO
        }
    }

    // ===========================================================
    // VALIDACIONES
    // ===========================================================

    private void validateDto(ProductDto dto) {

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto no puede estar vacío");
        }

        if (dto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        if (dto.getCostPrice() < 0) {
            throw new RuntimeException("El precio de costo no puede ser negativo");
        }

        if (dto.getSalePrice() < 0) {
            throw new RuntimeException("El precio de venta no puede ser negativo");
        }

        if (dto.getBranchId() == null) {
            throw new RuntimeException("La sucursal es obligatoria");
        }

        if (dto.getCategoryId() == null) {
            throw new RuntimeException("La categoría es obligatoria");
        }
    }
}
