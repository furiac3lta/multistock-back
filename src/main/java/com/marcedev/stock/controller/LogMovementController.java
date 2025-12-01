package com.marcedev.stock.controller;

import com.marcedev.stock.dto.LogMovementDto;
import com.marcedev.stock.mapper.LogMovementMapper;
import com.marcedev.stock.repository.LogMovementRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogMovementController {

    private final LogMovementRepository repo;
    private final LogMovementMapper mapper;

    // 🔥 Obtener TODO el historial
    @GetMapping
    public List<LogMovementDto> all() {
        return mapper.toDtoList(repo.findAllByOrderByCreatedAtDesc());
    }

    // 🔥 Filtrar por producto
    @GetMapping("/product/{id}")
    public List<LogMovementDto> byProduct(@PathVariable Long id) {
        return mapper.toDtoList(repo.findByProductIdOrderByCreatedAtDesc(id));
    }

    // 🔥 Filtrar por sucursal
    @GetMapping("/branch/{id}")
    public List<LogMovementDto> byBranch(@PathVariable Long id) {
        return mapper.toDtoList(repo.findByBranchIdOrderByCreatedAtDesc(id));
    }

    // 🔥 Filtrar por usuario
    @GetMapping("/user/{username}")
    public List<LogMovementDto> byUser(@PathVariable String username) {
        return mapper.toDtoList(repo.findByUsernameOrderByCreatedAtDesc(username));
    }
}
