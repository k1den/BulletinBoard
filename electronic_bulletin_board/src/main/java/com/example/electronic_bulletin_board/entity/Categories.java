package com.example.electronic_bulletin_board.entity;

import jakarta.persistence.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Entity
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор категории", example = "1")
    private Integer id;

    @Column(nullable = false)
    @Schema(description = "Название категории", example = "Электроника")
    private String title;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    @Schema(description = "Родительская категория (если есть)", example = "null")
    private Categories parentCategory;
}