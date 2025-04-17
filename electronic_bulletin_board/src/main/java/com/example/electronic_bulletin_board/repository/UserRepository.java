package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByLogin(String login);
    Optional<Users> findByEmail(String email);
}