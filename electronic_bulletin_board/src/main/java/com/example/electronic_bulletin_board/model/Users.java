package com.example.electronic_bulletin_board.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String login;
    private String password;
    private String role;
}
