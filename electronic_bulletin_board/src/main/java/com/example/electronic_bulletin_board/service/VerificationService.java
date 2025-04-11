package com.example.electronic_bulletin_board.service;

import com.example.electronic_bulletin_board.entity.UserVerification;
import com.example.electronic_bulletin_board.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    @Autowired
    private UserVerificationRepository verificationRepository;

    public boolean verifyCode(String email, String code) {
        UserVerification userVerification = verificationRepository.findByEmail(email);
        if (userVerification != null && userVerification.getVerificationCode().equals(code)) {
            // Верификация пройдена
            return true;
        }
        // Верификация не пройдена
        return false;
    }
}