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

    private String action;          // TRANSFERENCIA, AJUSTE, ALTA, BAJA
    private Long productId;

    private Long branchId;          // sucursal donde ocurrió
    private String branchName;

    private Double beforeStock;     // stock antes
    private Double afterStock;      // stock después

    private String username;        // usuario que ejecutó
    private String ip;              // IP origen

    private LocalDateTime createdAt; // timestamp
}
