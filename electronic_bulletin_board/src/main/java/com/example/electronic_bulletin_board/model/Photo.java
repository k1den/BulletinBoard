package com.example.electronic_bulletin_board.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Blob;

@Data
@Entity(name = "Photo")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String photo;

    @ManyToOne
    @JoinColumn(name = "ad_id")
    private Ads ad;
}
