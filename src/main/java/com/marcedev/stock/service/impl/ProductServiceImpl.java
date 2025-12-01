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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final ProductMapper mapper;

    // --------------------------------------------
    // NORMALIZAR NOMBRE DE SUCURSAL
    // --------------------------------------------
    private String normalizeBranch(String b) {
        if (b == null) return null;

        b = b.trim().toLowerCase();
        return switch (b) {
            case "centro" -> "Sucursal Centro";
            case "norte" -> "Sucursal Norte";
            case "sur" -> "Sucursal Sur";
            default -> b;
        };
    }

    // --------------------------------------------
    // CRUD
    // --------------------------------------------
    @Override
    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    public List<ProductDto> findByBranch(Long branchId) {
        return repository.findByBranchId(branchId).stream().map(mapper::toDto).toList();
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

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

        product.setCategory(category);
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

    // ---------------------------------------------------
    // IMPORTACIÓN DESDE EXCEL → COMPLETAMENTE CORREGIDA
    // ---------------------------------------------------
    @Transactional
    public void importProducts(List<ImportProductDTO> list) {

        System.out.println("\n====== JSON RECIBIDO ======");
        list.forEach(System.out::println);

        System.out.println("== INICIO IMPORTACIÓN, FILAS RECIBIDAS: " + list.size());

        for (ImportProductDTO dto : list) {

            System.out.println("\nProcesando fila: " + dto);

            // VALIDACIÓN DE CAMPOS OBLIGATORIOS
            if (dto.getName() == null || dto.getSku() == null ||
                    dto.getCategory() == null || dto.getBranch() == null) {
                throw new RuntimeException("Faltan campos obligatorios en el Excel");
            }

            // --- CATEGORÍA ---
            String categoryName = dto.getCategory().trim();
            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoryName));

            // --- SUCURSAL ---
            String branchName = normalizeBranch(dto.getBranch());
            Branch branch = branchRepository.findByNameIgnoreCase(branchName)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada: " + branchName));

            // --- SKU ---
            String cleanSku = dto.getSku().trim().toUpperCase();

            // Buscar producto por SKU + sucursal
            Optional<Product> opt = repository.findBySkuAndBranch(cleanSku, branch.getId());

            // ===================================================================================
            // SI EL PRODUCTO YA EXISTE → SE ACTUALIZA Y SE SUMA STOCK
            // ===================================================================================
            if (opt.isPresent()) {

                Product existing = opt.get();

                System.out.println("→ Producto encontrado: " + existing.getName() +
                        " | Stock actual: " + existing.getStock());

                // evitar nulls
                int stockActual = existing.getStock() == null ? 0 : existing.getStock();
                int stockImport = dto.getStock() == null ? 0 : dto.getStock();

                existing.setStock(stockActual + stockImport);
                existing.setCostPrice(dto.getCost() == null ? 0 : dto.getCost());
                existing.setSalePrice(dto.getPrice() == null ? 0 : dto.getPrice());
                existing.setCategory(category);
                existing.setBranch(branch);

                repository.save(existing);

                System.out.println("→ Nuevo stock: " + existing.getStock());
                continue;
            }

            // ===================================================================================
            // SI NO EXISTE → SE CREA NUEVO
            // ===================================================================================
            Product p = new Product();
            p.setName(dto.getName());
            p.setSku(cleanSku);
            p.setStock(dto.getStock() == null ? 0 : dto.getStock());
            p.setCostPrice(dto.getCost() == null ? 0 : dto.getCost());
            p.setSalePrice(dto.getPrice() == null ? 0 : dto.getPrice());
            p.setCategory(category);
            p.setBranch(branch);
            p.setActive(true);

            repository.save(p);

            System.out.println("→ Producto creado: " + dto.getName());
        }
    }

    // --------------------------------------------
    // VALIDACIONES
    // --------------------------------------------
    private void validateDto(ProductDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new RuntimeException("El nombre del producto no puede estar vacío");

        if (dto.getStock() < 0)
            throw new RuntimeException("El stock no puede ser negativo");

        if (dto.getCostPrice() < 0)
            throw new RuntimeException("El precio de costo no puede ser negativo");

        if (dto.getSalePrice() < 0)
            throw new RuntimeException("El precio de venta no puede ser negativo");

        if (dto.getBranchId() == null)
            throw new RuntimeException("La sucursal es obligatoria");

        if (dto.getCategoryId() == null)
            throw new RuntimeException("La categoría es obligatoria");
    }
}
