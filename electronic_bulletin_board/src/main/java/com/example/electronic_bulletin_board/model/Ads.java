package com.example.electronic_bulletin_board.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Entity(name = "Ads")
public class Ads {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "idCategory")
    private Categories idCategory;

    private String description;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "idUser")
    private Users idUsers;

    @ManyToOne
    @JoinColumn(name = "loginUser")
    private Users loginUsers;

    private Date dateOfCreation;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> idPhoto;
}
