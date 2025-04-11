package com.example.electronic_bulletin_board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("louisecaezgf0@rambler.ru");
        message.setTo(toEmail);
        message.setSubject("Email Verification Code");
        message.setText("Ваш код для проверки на сайте: " + verificationCode);
        mailSender.send(message);
    }
}