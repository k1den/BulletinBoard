package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.LoginRequest;
import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.entity.UserVerification;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import com.example.electronic_bulletin_board.repository.UserVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserVerificationRepository userVerificationRepository;

    @InjectMocks
    private AuthService authService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Users user;
    private UserVerification userVerification;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setLogin("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setRole("USER");
        registerRequest.setPhone("+79780010203");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = new Users();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("USER");
        user.setPhone("+79780010203");

        userVerification = new UserVerification();
        userVerification.setEmail("test@example.com");
        userVerification.setVerificationCode("123456");
    }

    @Test
    void register_Success() {
        when(userRepository.save(any(Users.class))).thenReturn(user);

        Users result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getLogin());
        assertTrue(passwordEncoder.matches("password123", result.getPassword()));
        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        Users result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertTrue(authService.isSessionActive());
        assertEquals("testuser", authService.getCurrentLogin());
    }

    @Test
    void login_WrongPassword() {
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        Users result = authService.login(loginRequest);

        assertNull(result);
        assertFalse(authService.isSessionActive());
        assertNull(authService.getCurrentLogin());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        Users result = authService.login(loginRequest);

        assertNull(result);
        assertFalse(authService.isSessionActive());
        assertNull(authService.getCurrentLogin());
    }

    @Test
    void logout_Success() {
        // Настраиваем мок для успешного входа
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        // Выполняем вход
        Users loginResult = authService.login(loginRequest);
        assertNotNull(loginResult);
        assertTrue(authService.isSessionActive());

        // Выполняем выход
        boolean logoutResult = authService.logout();
        assertTrue(logoutResult);
        assertFalse(authService.isSessionActive());
        assertNull(authService.getCurrentLogin());
    }

    @Test
    void logout_NoActiveSession() {
        assertFalse(authService.logout());
    }

    @Test
    void saveVerificationCode_NewUser() {
        when(userVerificationRepository.findByEmail("test@example.com")).thenReturn(null);

        authService.saveVerificationCode("test@example.com", "123456");

        verify(userVerificationRepository, times(1)).save(any(UserVerification.class));
    }

    @Test
    void saveVerificationCode_ExistingUser() {
        when(userVerificationRepository.findByEmail("test@example.com")).thenReturn(userVerification);

        authService.saveVerificationCode("test@example.com", "654321");

        assertEquals("654321", userVerification.getVerificationCode());
        verify(userVerificationRepository, times(1)).save(userVerification);
    }

    @Test
    void updateVerificationCode_UserExists() {
        when(userVerificationRepository.findByEmail("test@example.com")).thenReturn(userVerification);

        authService.updateVerificationCode("test@example.com", "newcode");

        assertEquals("newcode", userVerification.getVerificationCode());
        verify(userVerificationRepository, times(1)).save(userVerification);
    }

    @Test
    void updateVerificationCode_UserNotExists() {
        when(userVerificationRepository.findByEmail("test@example.com")).thenReturn(null);

        authService.updateVerificationCode("test@example.com", "newcode");

        verify(userVerificationRepository, never()).save(any());
    }

    @Test
    void validateRegistration_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(userRepository.findByLogin("testuser")).thenReturn(null);

        Map<String, Object> result = authService.validateRegistration(registerRequest);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void validateRegistration_InvalidEmail() {
        registerRequest.setEmail("invalid-email");

        Map<String, Object> result = authService.validateRegistration(registerRequest);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Некорректный email", result.get("message"));
    }

    @Test
    void validateRegistration_InvalidPhone() {
        registerRequest.setPhone("invalid-phone");

        Map<String, Object> result = authService.validateRegistration(registerRequest);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Некорректный номер телефона", result.get("message"));
    }

    @Test
    void validateRegistration_InvalidPassword() {
        registerRequest.setPassword("short");

        Map<String, Object> result = authService.validateRegistration(registerRequest);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Пароль должен содержать не менее 6 символов", result.get("message"));
    }

    @Test
    void validateRegistration_EmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        Map<String, Object> result = authService.validateRegistration(registerRequest);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Пользователь с такой почтой уже существует", result.get("message"));
    }

    @Test
    void validateRegistration_LoginExists() {
        when(userRepository.findByLogin("testuser")).thenReturn(user);

        Map<String, Object> result = authService.validateRegistration(registerRequest);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Пользователь с таким логином уже существует", result.get("message"));
    }
}