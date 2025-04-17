package com.example.electronic_bulletin_board.handler;

import com.example.electronic_bulletin_board.config.JwtUtils;
import com.example.electronic_bulletin_board.entity.CustomOAuth2User;
import com.example.electronic_bulletin_board.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;

    public OAuth2SuccessHandler(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // Основной метод для обработки успешной аутентификации
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        handleAuthenticationSuccess(request, response, authentication);
    }

    // Новый метод в Spring Security 6+
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain,
                                        Authentication authentication) throws IOException, ServletException {
        handleAuthenticationSuccess(request, response, authentication);
        chain.doFilter(request, response);
    }

    private void handleAuthenticationSuccess(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomOAuth2User)) {
            throw new IllegalStateException("Principal is not of type CustomOAuth2User");
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
        Users user = oAuth2User.getUser();

        // Генерируем JWT токен
        String jwt = jwtUtils.generateToken(user);

        // Переадресовываем пользователя на метод oauthSuccess с токеном
        response.sendRedirect("/api/auth/oauth-success?token=" + jwt);
    }

}