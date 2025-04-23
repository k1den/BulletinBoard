package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.LoginRequest;
import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.entity.UserVerification;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import com.example.electronic_bulletin_board.repository.UserVerificationRepository;
import com.example.electronic_bulletin_board.validators.AuthValidator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    private boolean session = false;
    @Getter
    private String currentLogin = null;
    @Getter
    private String currentPassword = null;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Users register(RegisterRequest registerRequest) {
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

    public void saveVerificationCode(String email, String verificationCode) {
        UserVerification user = userVerificationRepository.findByEmail(email);
        if (user == null) {
            user = new UserVerification();
            user.setEmail(email);
        }
        user.setVerificationCode(verificationCode);
        userVerificationRepository.save(user);
    }

    public void updateVerificationCode(String email, String newCode) {
        UserVerification user = userVerificationRepository.findByEmail(email);
        if (user != null) {
            user.setVerificationCode(newCode);
            userVerificationRepository.save(user);
        }
    }

    public Map<String, Object> validateRegistration(RegisterRequest registerRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Валидация email
            if (!AuthValidator.isValidEmail(registerRequest.getEmail())) {
                throw new RuntimeException("Некорректный email");
            }

            // Валидация номера телефона
            if (registerRequest.getPhone() != null && !registerRequest.getPhone().isEmpty()) {
                if (!AuthValidator.isValidPhoneNumber(registerRequest.getPhone())) {
                    throw new RuntimeException("Некорректный номер телефона");
                }
            }

            // Валидация пароля
            if (!AuthValidator.isValidPassword(registerRequest.getPassword())) {
                throw new RuntimeException("Пароль должен содержать не менее 6 символов");
            }

            // Проверка совпадения паролей
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                throw new RuntimeException("Пароли не совпадают");
            }

            // Валидация, существует ли юзер с таким email или логином
            if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
                throw new RuntimeException("Пользователь с такой почтой уже существует");
            }
            if (userRepository.findByLogin(registerRequest.getLogin()) != null) {
                throw new RuntimeException("Пользователь с таким логином уже существует");
            }

            response.put("success", true);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }
}
