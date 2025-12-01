package com.marcedev.stock.service.impl;

import com.marcedev.stock.dto.StockMovementDto;
import com.marcedev.stock.dto.StockTransferRequest;
import com.marcedev.stock.entity.*;
import com.marcedev.stock.mapper.StockMovementMapper;
import com.marcedev.stock.repository.*;
import com.marcedev.stock.service.StockMovementService;
import com.marcedev.stock.service.LogMovementService;

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

    private final LogMovementService logService;
    private final LogMovementRepository logMovementRepository;

    // ==========================================================
    // LISTADOS
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
    // AJUSTE NORMAL DE STOCK
    // ==========================================================
    @Override
    public StockMovementDto move(Long productId, Integer quantity, String type, String description, String user) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Branch branch = product.getBranch();

        int previousStock = product.getStock();
        int newStock;

        MovementType movementType = MovementType.valueOf(type);

        if (movementType == MovementType.INCREASE) {
            newStock = previousStock + quantity;
        } else {
            newStock = previousStock - quantity;
            if (newStock < 0)
                throw new RuntimeException("El stock no puede quedar negativo");
        }

        product.setStock(newStock);
        productRepo.save(product);

        // 🔥 Movimiento
        StockMovement movement = StockMovement.builder()
                .product(product)
                .branch(branch)
                .quantity(quantity)
                .movementType(movementType)
                .description(description)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build();

        movementRepo.save(movement);

        // 🔥 Log completo
        LogMovement log = LogMovement.builder()
                .productId(product.getId())
                .productName(product.getName())

                .movementType("ADJUST_" + type)
                .quantity((double) quantity)
                .description(description)

                .branchId(branch.getId())
                .branchName(branch.getName())

                .beforeStock((double) previousStock)
                .afterStock((double) newStock)

                .username(user)
                .ip("N/A")
                .createdAt(LocalDateTime.now())
                .build();

        saveLog(log);

        StockMovementDto dto = mapper.toDto(movement);
        dto.setPreviousStock(previousStock);
        dto.setNewStock(newStock);

        return dto;
    }

    // ==========================================================
    // TRANSFERENCIA ENTRE SUCURSALES
    // ==========================================================
    @Override
    public StockMovementDto transfer(StockTransferRequest req) {

        Branch source = branchRepo.findById(req.getSourceBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal origen no encontrada"));

        Branch target = branchRepo.findById(req.getTargetBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

        if (source.getId().equals(target.getId()))
            throw new RuntimeException("No puedes transferir dentro de la misma sucursal");

        if (req.getQuantity() <= 0)
            throw new RuntimeException("La cantidad debe ser mayor a 0");

        Product product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!product.getBranch().getId().equals(source.getId()))
            throw new RuntimeException("El producto no pertenece a la sucursal origen");

        // STOCK ORIGEN
        int previousSourceStock = product.getStock();
        int newSourceStock = previousSourceStock - req.getQuantity();

        if (newSourceStock < 0)
            throw new RuntimeException("Stock insuficiente en sucursal origen");

        product.setStock(newSourceStock);
        productRepo.save(product);

        // PRODUCTO EN DESTINO
        Product destino = productRepo.findBySkuAndBranch(product.getSku(), target.getId());
        if (destino == null) {
            destino = Product.builder()
                    .name(product.getName())
                    .sku(product.getSku())
                    .category(product.getCategory())
                    .costPrice(product.getCostPrice())
                    .salePrice(product.getSalePrice())
                    .branch(target)
                    .stock(0)
                    .active(true)
                    .build();
        }

        int previousDestStock = destino.getStock();
        int newDestStock = previousDestStock + req.getQuantity();

        destino.setStock(newDestStock);
        productRepo.save(destino);

        // 🔥 MOVIMIENTO ORIGEN
        StockMovement movementOut = StockMovement.builder()
                .branch(source)
                .product(product)
                .quantity(req.getQuantity())
                .movementType(MovementType.DECREASE)
                .description(req.getDescription())
                .createdBy(req.getUser())
                .createdAt(LocalDateTime.now())
                .build();

        movementRepo.save(movementOut);

        // 🔥 MOVIMIENTO DESTINO
        StockMovement movementIn = StockMovement.builder()
                .branch(target)
                .product(destino)
                .quantity(req.getQuantity())
                .movementType(MovementType.INCREASE)
                .description(req.getDescription())
                .createdBy(req.getUser())
                .createdAt(LocalDateTime.now())
                .build();

        movementRepo.save(movementIn);

        // 🔥 LOG ORIGEN
        saveLog(LogMovement.builder()
                .productId(product.getId())
                .productName(product.getName())
                .movementType("TRANSFER_OUT")
                .quantity((double) req.getQuantity())
                .description(req.getDescription())
                .branchId(source.getId())
                .branchName(source.getName())
                .originBranchId(source.getId())
                .originBranchName(source.getName())
                .beforeStock((double) previousSourceStock)
                .afterStock((double) newSourceStock)
                .username(req.getUser())
                .ip(req.getIp())
                .createdAt(LocalDateTime.now())
                .build());

        // 🔥 LOG DESTINO
        saveLog(LogMovement.builder()
                .productId(destino.getId())
                .productName(destino.getName())
                .movementType("TRANSFER_IN")
                .quantity((double) req.getQuantity())
                .description(req.getDescription())
                .branchId(target.getId())
                .branchName(target.getName())
                .originBranchId(source.getId())
                .originBranchName(source.getName())
                .beforeStock((double) previousDestStock)
                .afterStock((double) newDestStock)
                .username(req.getUser())
                .ip(req.getIp())
                .createdAt(LocalDateTime.now())
                .build());

        return mapper.toDto(movementOut);
    }


    // ==========================================================
    // MÉTODO INTERNO PARA GUARDAR LOG
    // ==========================================================
    private void saveLog(LogMovement log) {
        log.setCreatedAt(LocalDateTime.now());
        logMovementRepository.save(log);
    }
}
