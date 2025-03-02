package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.model.Ads;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertisementRepository extends JpaRepository<Ads, Integer>  {
}
