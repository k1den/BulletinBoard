package com.example.electronic_bulletin_board.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "The unique identifier of the user")
    private Integer id;

    @Schema(description = "The email of the user", example = "user@example.com")
    private String email;

    @Schema(description = "The login of the user", example = "user123")
    private String login;

    @Schema(description = "The password of the user", example = "password123")
    private String password;

    @Schema(description = "The role of the user", example = "USER")
    private String role;
}