package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.entity.Ads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Ads, Integer>  {
    @Query("SELECT a FROM Ads a JOIN a.idCategory c WHERE c.title = :categoryName")
    List<Ads> findByIdCategory_Title(@Param("categoryName") String categoryName);
}
