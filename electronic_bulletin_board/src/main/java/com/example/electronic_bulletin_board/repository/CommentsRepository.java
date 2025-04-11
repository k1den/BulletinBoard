package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Integer> {
    List<Comments> findByAdsId(Integer adsId);
    List<Comments> findByUserId(Integer userId);
}