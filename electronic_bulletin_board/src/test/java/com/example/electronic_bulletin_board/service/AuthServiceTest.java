package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.dto.LoginRequest;
import com.example.electronic_bulletin_board.dto.RegisterRequest;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private Users existingUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setLogin("testuser");
        validRegisterRequest.setPassword("Password123");
        validRegisterRequest.setConfirmPassword("Password123");
        validRegisterRequest.setRole("USER");
        validRegisterRequest.setPhone("+79780010203");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("Password123");

        existingUser = new Users();
        existingUser.setEmail("test@example.com");
        existingUser.setLogin("testuser");
        existingUser.setPassword(passwordEncoder.encode("Password123"));
        existingUser.setRole("USER");
    }

    // Тест регистрации с валидными данными
    @Test
    void register_WithValidData_ReturnsUser() {
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(null);
        when(userRepository.findByLogin(validRegisterRequest.getLogin())).thenReturn(null);
        when(userRepository.save(any(Users.class))).thenReturn(existingUser);

        Users result = authService.register(validRegisterRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getLogin());
        assertTrue(passwordEncoder.matches("Password123", result.getPassword()));
        verify(userRepository, times(1)).save(any(Users.class));
    }

    // Тест регистрации с некорректным email
    @Test
    void register_WithInvalidEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("Password123");
        request.setConfirmPassword("Password123");

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    // Тест регистрации с некорректным паролем
    @Test
    void register_WithInvalidPassword_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("short");
        request.setConfirmPassword("short");

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    // Тест регистрации с несовпадающими паролями
    @Test
    void register_WithPasswordMismatch_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        request.setConfirmPassword("Different123");

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    // Тест регистрации с уже существующим email
    @Test
    void register_WithExistingEmail_ThrowsException() {
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(existingUser);

        assertThrows(RuntimeException.class, () -> authService.register(validRegisterRequest));
    }

    // Тест регистрации с уже существующим логином
    @Test
    void register_WithExistingLogin_ThrowsException() {
        when(userRepository.findByEmail(validRegisterRequest.getEmail())).thenReturn(null);
        when(userRepository.findByLogin(validRegisterRequest.getLogin())).thenReturn(existingUser);

        assertThrows(RuntimeException.class, () -> authService.register(validRegisterRequest));
    }

    // Тест входа с валидными данными
    @Test
    void login_WithValidCredentials_ReturnsUserAndSetsSession() {
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(existingUser);

        Users result = authService.login(validLoginRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertTrue(authService.isSessionActive());
        assertEquals("testuser", authService.getCurrentLogin());
    }

    // Тест входа с некорректным email
    @Test
    void login_WithInvalidEmail_ReturnsNull() {
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(null);

        Users result = authService.login(validLoginRequest);

        assertNull(result);
        assertFalse(authService.isSessionActive());
        assertNull(authService.getCurrentLogin());
    }

    // Тест входа с некорректным паролем
    @Test
    void login_WithInvalidPassword_ReturnsNull() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(existingUser);

        Users result = authService.login(request);

        assertNull(result);
        assertFalse(authService.isSessionActive());
        assertNull(authService.getCurrentLogin());
    }

    // Тест выхода из системы, когда сессия неактивна
    @Test
    void logout_WhenSessionInactive_ReturnsFalse() {
        assertFalse(authService.logout());
    }

    // Тест проверки активности сессии, когда пользователь не авторизован
    @Test
    void isSessionActive_WhenLoggedOut_ReturnsFalse() {
        assertFalse(authService.isSessionActive());
    }
}
