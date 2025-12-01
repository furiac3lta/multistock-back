package com.marcedev.stock.service.impl;

import com.marcedev.stock.entity.LogMovement;
import com.marcedev.stock.repository.LogMovementRepository;
import com.marcedev.stock.service.LogMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogMovementServiceImpl implements LogMovementService {

    private final LogMovementRepository repo;

    @Override
    public void save(LogMovement log) {
        log.setCreatedAt(LocalDateTime.now());
        repo.save(log);
    }
}
