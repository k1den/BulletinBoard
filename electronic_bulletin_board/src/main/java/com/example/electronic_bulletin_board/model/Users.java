package com.example.electronic_bulletin_board.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор пользователя")
    private Integer id;

    @Schema(description = "Почта пользователя")
    private String email;

    @Schema(description = "Логин пользователя")
    private String login;

    @Schema(description = "Пароль пользователя")
    private String password;

    @Schema(description = "Роль пользователя")
    private String role;

    @Schema(description = "Телефон пользователя")
    private String phone;

    @Transient
    @Schema(description = "Повтор пароля")
    private String confirmPassword;
}