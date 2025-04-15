package com.example.electronic_bulletin_board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @Operation(
            summary = "Получить защищённые данные",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "Требуется валидный JWT токен в заголовке Authorization"
    )
    @GetMapping("/data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> getProtectedData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("Доступ запрещён");
        }

        String username = authentication.getName();

        System.out.println("Доступ предоставлен пользователю: " + username);

        return ResponseEntity.ok("Доступ разрешён для пользователя: " + username);
    }
}