package com.marcedev.stock.controller;

import com.marcedev.stock.dto.StockMovementDto;
import com.marcedev.stock.dto.StockTransferRequest;
import com.marcedev.stock.service.StockMovementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    // ==========================================================
    // LISTADO COMPLETO
    // ==========================================================
    @GetMapping("/movements")
    public List<StockMovementDto> all() {
        return stockMovementService.findAll();
    }
    @GetMapping("/all")
    public List<StockMovementDto> allMovements() {
        return stockMovementService.findAll();
    }

    // ==========================================================
    // HISTORIAL DE UN PRODUCTO
    // ==========================================================
    @GetMapping("/history/{productId}")
    public List<StockMovementDto> history(@PathVariable Long productId) {
        return stockMovementService.history(productId);
    }

    // ==========================================================
    // HISTORIAL POR SUCURSAL
    // ==========================================================
    @GetMapping("/history/branch/{branchId}")
    public List<StockMovementDto> historyByBranch(@PathVariable Long branchId) {
        return stockMovementService.historyByBranch(branchId);
    }

    // ==========================================================
    // AJUSTE NORMAL (INCREASE / DECREASE)
    // ==========================================================
    @PostMapping("/move")
    public StockMovementDto move(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam String user,
            HttpServletRequest request
    ) {
        return stockMovementService.move(productId, quantity, type, description, user);
    }

    // ==========================================================
    // TRANSFERENCIA ENTRE SUCURSALES
    // ==========================================================
    @PostMapping("/transfer")
    public StockMovementDto transfer(
            @RequestBody StockTransferRequest req,
            HttpServletRequest request
    ) {
        // Agregamos IP del cliente en el request
        req.setIp(request.getRemoteAddr());

        return stockMovementService.transfer(req);
    }

    @PostMapping("/{productId}/move")
    public StockMovementDto moveWithPath(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam String type,
            @RequestParam(required = false) String description,
            @RequestParam String user
    ) {
        return stockMovementService.move(productId, quantity, type, description, user);
    }

}
