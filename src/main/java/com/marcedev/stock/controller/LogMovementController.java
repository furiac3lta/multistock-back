package com.marcedev.stock.controller;

import com.marcedev.stock.entity.LogMovement;
import com.marcedev.stock.repository.LogMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogMovementController {

    private final LogMovementRepository repo;

    // 🔥 Obtener TODO el historial
    @GetMapping
    public List<LogMovement> all() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    // 🔥 Filtrar por producto
    @GetMapping("/product/{id}")
    public List<LogMovement> byProduct(@PathVariable Long id) {
        return repo.findByProductIdOrderByCreatedAtDesc(id);
    }

    // 🔥 Filtrar por sucursal
    @GetMapping("/branch/{id}")
    public List<LogMovement> byBranch(@PathVariable Long id) {
        return repo.findByBranchIdOrderByCreatedAtDesc(id);
    }

    // 🔥 Filtrar por usuario
    @GetMapping("/user/{username}")
    public List<LogMovement> byUser(@PathVariable String username) {
        return repo.findByUsernameOrderByCreatedAtDesc(username);
    }
}
