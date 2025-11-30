package com.marcedev.stock.service.impl;

import com.marcedev.stock.dto.StockMovementDto;
import com.marcedev.stock.dto.StockTransferRequest;
import com.marcedev.stock.entity.Branch;
import com.marcedev.stock.entity.MovementType;
import com.marcedev.stock.entity.Product;
import com.marcedev.stock.entity.StockMovement;
import com.marcedev.stock.mapper.StockMovementMapper;
import com.marcedev.stock.repository.BranchRepository;
import com.marcedev.stock.repository.ProductRepository;
import com.marcedev.stock.repository.StockMovementRepository;
import com.marcedev.stock.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository movementRepo;
    private final ProductRepository productRepo;
    private final BranchRepository branchRepo;
    private final StockMovementMapper mapper;

    // ==========================================================
    //                      LISTADOS
    // ==========================================================
    @Override
    public List<StockMovementDto> findAll() {
        return movementRepo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<StockMovementDto> history(Long productId) {
        return movementRepo.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<StockMovementDto> historyByBranch(Long branchId) {
        return movementRepo.findByProductBranchIdOrderByCreatedAtDesc(branchId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ==========================================================
    //                AJUSTE NORMAL DE STOCK
    // ==========================================================
    @Override
    public StockMovementDto move(Long productId, Integer quantity, String type, String description, String user) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int previousStock = product.getStock();
        int newStock;

        MovementType movementType = MovementType.valueOf(type);

        if (movementType == MovementType.INCREASE) {
            newStock = previousStock + quantity;
        } else {
            newStock = previousStock - quantity;
            if (newStock < 0) throw new RuntimeException("El stock no puede quedar negativo");
        }

        product.setStock(newStock);
        productRepo.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .branch(product.getBranch())      // ✅ FIX CRÍTICO
                .quantity(quantity)
                .movementType(movementType)
                .description(description)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        movementRepo.save(movement);

        StockMovementDto dto = mapper.toDto(movement);
        dto.setPreviousStock(previousStock);
        dto.setNewStock(newStock);

        return dto;
    }

    // ==========================================================
    //            TRANSFERENCIA ENTRE SUCURSALES
    // ==========================================================
    @Override
    public StockMovementDto transfer(StockTransferRequest req) {

        // VALIDAR SUCURSALES
        Branch source = branchRepo.findById(req.getSourceBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal origen no encontrada"));

        Branch target = branchRepo.findById(req.getTargetBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

        if (source.getId().equals(target.getId())) {
            throw new RuntimeException("No puedes transferir dentro de la misma sucursal");
        }

        if (req.getQuantity() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        // PRODUCTO ORIGEN
        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!product.getBranch().getId().equals(source.getId())) {
            throw new RuntimeException("El producto no pertenece a la sucursal origen");
        }

        // RESTAR STOCK ORIGEN
        int newSourceStock = product.getStock() - req.getQuantity();
        if (newSourceStock < 0) {
            throw new RuntimeException("Stock insuficiente en sucursal origen");
        }

        product.setStock(newSourceStock);
        productRepo.save(product);

        // ===============================================
        //        PRODUCTO EN SUCURSAL DESTINO
        // ===============================================
        String sku = product.getSku();

        Product destino = productRepo.findBySkuAndBranch(sku, target.getId());

        if (destino == null) {
            destino = Product.builder()
                    .name(product.getName())
                    .sku(product.getSku())
                    .category(product.getCategory())
                    .costPrice(product.getCostPrice())
                    .salePrice(product.getSalePrice())
                    .branch(target) // 🔥 NECESARIO
                    .stock(0)
                    .active(true)
                    .build();
        }

        destino.setStock(destino.getStock() + req.getQuantity());
        productRepo.save(destino);

        // ===============================================
        //     REGISTRAR MOVIMIENTO EN ORIGEN
        // ===============================================
        StockMovement movementOut = StockMovement.builder()
                .branch(source) // 🔥 FIX 1 — AGREGAR BRANCH ORIGEN
                .product(product)
                .quantity(req.getQuantity())
                .movementType(MovementType.DECREASE)
                .description("Transferencia a sucursal: " + target.getName())
                .createdBy(req.getUser())
                .createdAt(LocalDateTime.now())
                .build();

        movementRepo.save(movementOut);

        // ===============================================
        //     REGISTRAR MOVIMIENTO EN DESTINO
        // ===============================================
        StockMovement movementIn = StockMovement.builder()
                .branch(target) // 🔥 FIX 2 — AGREGAR BRANCH DESTINO
                .product(destino)
                .quantity(req.getQuantity())
                .movementType(MovementType.INCREASE)
                .description("Transferencia desde sucursal: " + source.getName())
                .createdBy(req.getUser())
                .createdAt(LocalDateTime.now())
                .build();

        movementRepo.save(movementIn);

        return mapper.toDto(movementOut);
    }

}
