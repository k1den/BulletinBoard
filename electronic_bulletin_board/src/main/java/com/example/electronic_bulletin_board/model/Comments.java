package com.example.electronic_bulletin_board.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "ads_id")
    private Ads ads;
}