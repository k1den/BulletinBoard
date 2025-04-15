package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import com.example.electronic_bulletin_board.validators.AuthValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Users register(RegisterRequest registerRequest) {
        Users user = new Users();
        user.setEmail(registerRequest.getEmail());
        user.setLogin(registerRequest.getLogin());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole());
        user.setPhone(registerRequest.getPhone());
        return userRepository.save(user);
    }
}
