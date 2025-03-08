package com.example.electronic_bulletin_board.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String login;
    private String password;
    private String role = "Пользователь";
}