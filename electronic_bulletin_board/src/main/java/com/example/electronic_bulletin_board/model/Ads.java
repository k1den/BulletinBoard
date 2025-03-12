package com.example.electronic_bulletin_board.model;

import jakarta.persistence.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Date;

@Data
@Entity(name = "Ads")
public class Ads {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "Уникальный идентификатор объявления")
    private Integer id;

    @Schema(description = "Заголовок объявления")
    private String title;

    @ManyToOne
    @JoinColumn(name = "id_category")
    @Schema(description = "Категория объявления")
    private Categories idCategory;

    @Schema(description = "Описание объявления")
    private String description;

    @Schema(description = "Цена")
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "id_user")
    @Schema(description = "id пользователя, создавшего объявление")
    private Users idUsers;

    @Schema(description = "Дата создания объявления")
    private Date date;

    @Lob
    @Schema(description = "Фотография объявления в виде бинарных данных")
    private byte[] photo;
}