package com.example.electronic_bulletin_board.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private Users userId;

    private BigDecimal totalPrice = BigDecimal.ZERO;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}