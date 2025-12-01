package com.marcedev.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== PRODUCTO =====
    private Long productId;
    private String productName;

    // ===== MOVIMIENTO =====
    private String movementType;      // ADJUST_INCREASE, ADJUST_DECREASE, TRANSFER_OUT, TRANSFER_IN
    private Double quantity;
    private String description;

    // ===== SUCURSALES =====
    private Long branchId;            // sucursal destino
    private String branchName;

    private Long originBranchId;      // en transferencias
    private String originBranchName;  // en transferencias

    // ===== STOCK =====
    private Double beforeStock;
    private Double afterStock;

    // ===== AUDITORÍA =====
    private String username;
    private String ip;
    private LocalDateTime createdAt;
}
