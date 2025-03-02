package com.example.electronic_bulletin_board.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categories") // Указываем имя таблицы в БД
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false) // Название категории не может быть null
    private String title;

    @ManyToOne
    @JoinColumn(name = "parent_category_id") // Указываем имя столбца в БД
    private Categories parentCategory; // Используем camelCase для названия поля
}