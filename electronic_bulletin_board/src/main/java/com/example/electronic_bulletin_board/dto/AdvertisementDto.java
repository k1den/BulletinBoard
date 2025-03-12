package com.example.electronic_bulletin_board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AdvertisementDto {

    @Schema(description = "Заголовок объявления")
    private String title;

    @Schema(description = "ID категории")
    private Integer idCategory;

    private String categoryTitle;

    @Schema(description = "Описание объявления")
    private String description;

    @Schema(description = "Цена")
    private BigDecimal price;

    @Schema(description = "ID пользователя")
    private Integer idUsers;

    @Schema(description = "Дата создания объявления")
    private Date date;

    @Schema(description = "Фотография объявления в виде бинарных данных")
    private byte[] photo;
}