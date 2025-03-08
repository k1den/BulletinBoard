package com.example.electronic_bulletin_board.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String login;
    private String password;
}