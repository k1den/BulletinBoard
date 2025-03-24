package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.LoginRequest;
import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.model.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import com.example.electronic_bulletin_board.validators.AuthValidator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private boolean session = false;
    @Getter
    private String currentLogin = null;
    @Getter
    private String currentPassword = null;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Users register(RegisterRequest registerRequest) {
        // Валидация email
        if (!AuthValidator.isValidEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Некорректный email");
        }

        // Валидация номера телефона (если он указан)
        if (registerRequest.getPhone() != null && !registerRequest.getPhone().isEmpty()) {
            if (!AuthValidator.isValidPhoneNumber(registerRequest.getPhone())) {
                throw new RuntimeException("Некорректный номер телефона");
            }
        }

        // Валидация пароля
        if (!AuthValidator.isValidPassword(registerRequest.getPassword())) {
            throw new RuntimeException("Пароль должен содержать не менее 6 символов");
        }

        // Повторяется ли?
        if (!AuthValidator.doPasswordsMatch(registerRequest.getPassword(), registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Пароли не совпадают");
        }

        // Проверяем, существует ли юзер с таким email
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("Пользователь с такой почтой уже существует");
        }

        // Проверяем, существует ли юзер с таким логином
        if (userRepository.findByLogin(registerRequest.getLogin()) != null) {
            throw new RuntimeException("Пользователь с таким логином уже существует");
        }

        // Создаем нового юзера
        Users user = new Users();
        user.setEmail(registerRequest.getEmail());
        user.setLogin(registerRequest.getLogin());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Хэшируем пароль
        user.setRole(registerRequest.getRole());
        user.setPhone(registerRequest.getPhone());
        return userRepository.save(user);
    }

    public Users login(LoginRequest loginRequest) {
        Users user = userRepository.findByEmail(loginRequest.getEmail());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) { // Проверяем пароль
            session = true;
            currentLogin = user.getLogin();
            currentPassword = user.getPassword();
            return user;
        }
        session = false;
        currentLogin = null;
        currentPassword = null;
        return null;
    }

    public boolean logout() {
        if (!session) {
            return false;
        }
        session = false;
        currentLogin = null;
        currentPassword = null;
        return true;
    }

    public boolean isSessionActive() {
        return session;
    }
}