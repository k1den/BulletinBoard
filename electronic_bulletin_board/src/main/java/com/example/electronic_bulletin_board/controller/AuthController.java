package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.config.JwtUtils;
import com.example.electronic_bulletin_board.dto.*;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import com.example.electronic_bulletin_board.service.AuthService;
import com.example.electronic_bulletin_board.service.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аунтификация", description = "API для входа и регистрации пользователя")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Регистрация нового пользователя по почте, логину и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            Users user = authService.register(registerRequest);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя", description = "Вход пользователя по email и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вошёл"),
            @ApiResponse(responseCode = "401", description = "Пользователь не существует/неверный логин или пароль")
    })
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        String jwt = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход пользователя", description = "Выход пользователя из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел"),
            @ApiResponse(responseCode = "400", description = "Пользователь не вошел в систему")
    })
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Пользователь успешно вышел из системы");
    }


    @Operation(summary = "Получить защищённые данные", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/protected-data")
    public ResponseEntity<String> getProtectedData() {
        return ResponseEntity.ok("Доступ разрешён");
    }
}