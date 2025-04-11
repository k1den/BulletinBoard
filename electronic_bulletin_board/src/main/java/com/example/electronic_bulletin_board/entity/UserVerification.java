package com.example.electronic_bulletin_board.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "UserVerification")
@Data
public class UserVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String verificationCode;
}
