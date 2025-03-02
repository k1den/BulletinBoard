package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Integer> {
}
