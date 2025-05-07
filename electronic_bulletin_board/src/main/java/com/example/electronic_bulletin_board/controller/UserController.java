package com.example.electronic_bulletin_board.controller;

import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.service.AuthService;
import com.example.electronic_bulletin_board.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public Object getAllUsers() {
        if (!authService.isSessionActive()) return Map.of("error", "unauthorized");
        Users currentUser = userService.getUserByLogin(authService.getCurrentLogin());
        if (currentUser == null) return Map.of("error", "unauthorized");
        if (!"Администратор".equals(currentUser.getRole())) return Map.of("error", "forbidden");

        return userService.getAllUsers();
    }

    @PostMapping("/change-role")
    public Object changeUserRole(@RequestParam Integer id, @RequestParam String role) {
        if (!authService.isSessionActive()) return Map.of("error", "unauthorized");
        Users currentUser = userService.getUserByLogin(authService.getCurrentLogin());
        if (currentUser == null || !"Администратор".equals(currentUser.getRole()))
            return Map.of("error", "forbidden");

        Users updated = userService.changeRole(id, role);
        return updated != null ? Map.of("success", true, "user", updated) : Map.of("success", false);
    }
}
