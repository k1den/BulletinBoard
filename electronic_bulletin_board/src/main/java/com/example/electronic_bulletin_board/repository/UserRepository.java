package com.example.electronic_bulletin_board.repository;

import com.example.electronic_bulletin_board.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findByLogin(String login);
    Users findByEmail(String email); // Новый метод для поиска по email
}