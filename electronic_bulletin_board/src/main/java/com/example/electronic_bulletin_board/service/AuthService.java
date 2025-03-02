package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.LoginRequest;
import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.model.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public Users register(RegisterRequest registerRequest) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("Пользователь с такой почтой уже существует");
        }

        // Проверяем, существует ли пользователь с таким логином
        if (userRepository.findByLogin(registerRequest.getLogin()) != null) {
            throw new RuntimeException("Пользователь с таким логином уже существует");
        }

        // Создаем нового пользователя
        Users user = new Users();
        user.setEmail(registerRequest.getEmail());
        user.setLogin(registerRequest.getLogin());
        user.setPassword(registerRequest.getPassword()); // Пароль сохраняется как есть
        user.setRole(registerRequest.getRole());
        return userRepository.save(user);
    }

    public Users login(LoginRequest loginRequest) {
        Users user = userRepository.findByLogin(loginRequest.getLogin());
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            return user;
        }
        return null;
    }
}