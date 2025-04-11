package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Integer> {
    UserVerification findByEmail(String email);
}

