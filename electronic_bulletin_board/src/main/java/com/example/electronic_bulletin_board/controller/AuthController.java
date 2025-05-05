package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.dto.LoginRequest;
import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.generator.VerificationCodeGenerator;
import com.example.electronic_bulletin_board.repository.UserRepository;
import com.example.electronic_bulletin_board.service.AuthService;
import com.example.electronic_bulletin_board.service.EmailService;
import com.example.electronic_bulletin_board.service.VerificationService;
import com.example.electronic_bulletin_board.validators.AuthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аунтификация", description = "API для входа и регистрации пользователя")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Регистрация нового пользователя по почте, логину и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<?> register(
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String confirmPassword) {
        try {
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(email);
            registerRequest.setPhone(phone);
            registerRequest.setLogin(login);
            registerRequest.setPassword(password);
            registerRequest.setConfirmPassword(confirmPassword);

            Users user = authService.register(registerRequest);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя", description = "Вход пользователя по логину и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вошёл"),
            @ApiResponse(responseCode = "401", description = "Пользователь не существует/неверный логин или пароль")
    })
    public ResponseEntity<Users> login(
            @RequestParam String email, // Используем email для входа
            @RequestParam String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        Users user = authService.login(loginRequest);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход пользователя", description = "Выход пользователя из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел"),
            @ApiResponse(responseCode = "400", description = "Пользователь не вошел в систему")
    })
    public ResponseEntity<String> logout() {
        boolean isLoggedOut = authService.logout();
        if (isLoggedOut) {
            return ResponseEntity.ok("Пользователь успешно вышел из системы");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: пользователь не вошел в систему");
        }
    }

    @GetMapping("/check-session")
    public ResponseEntity<String> checkSession() {
        if (authService.isSessionActive()) {
            return ResponseEntity.ok("Сессия активна. Текущий пользователь: " + authService.getCurrentLogin());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Сессия не активна");
        }
    }

    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = VerificationCodeGenerator.generateVerificationCode();

        authService.saveVerificationCode(email, verificationCode);

        emailService.sendVerificationEmail(email, verificationCode);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isVerified = verificationService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok().body(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Неверный код подтверждения"));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newCode = VerificationCodeGenerator.generateVerificationCode();

        authService.updateVerificationCode(email, newCode);

        emailService.sendVerificationEmail(email, newCode);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-registration")
    public ResponseEntity<Map<String, Object>> validateRegistration(@RequestBody RegisterRequest registerRequest) {
        Map<String, Object> response = authService.validateRegistration(registerRequest);
        return response.get("success").equals(true) ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}