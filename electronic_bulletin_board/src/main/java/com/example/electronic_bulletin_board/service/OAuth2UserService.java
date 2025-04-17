package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.entity.CustomOAuth2User;
import com.example.electronic_bulletin_board.entity.Users;
import com.example.electronic_bulletin_board.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2UserService(UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // GitHub-specific attribute extraction
        String login = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        if (email == null) {
            email = login + "@github.com"; // fallback if email is not provided
        }

        // Проверяем существующего пользователя
        String finalEmail = email;
        Users user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(finalEmail, login, name));

        return new CustomOAuth2User(user, attributes);
    }

    private Users createNewUser(String email, String login, String name) {
        Users newUser = new Users();
        newUser.setEmail(email);
        newUser.setLogin(login);
        newUser.setPassword(passwordEncoder.encode("oauth2password"));
        newUser.setRole("USER");
        return userRepository.save(newUser);
    }
}