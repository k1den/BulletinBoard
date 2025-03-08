package com.example.electronic_bulletin_board.validators;

import java.util.regex.Pattern;

public class AuthValidator {

    // Регулярное выражение для проверки email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    // Регулярное выражение для проверки номера телефона
    private static final String PHONE_REGEX = "^\\+[0-9]{11}$"; // Формат: +79781112233 (ровно 12 символов, включая +)

    // Минимальная длина пароля
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Проверяет, является ли email допустимым.
     *
     * @param email Email для проверки.
     * @return true, если email допустим, иначе false.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    /**
     * Проверяет, является ли номер телефона допустимым.
     * Если номер телефона пустой (null или пустая строка), он считается допустимым.
     *
     * @param phoneNumber Номер телефона для проверки.
     * @return true, если номер телефона допустим или пустой, иначе false.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Если номер телефона пустой, пропускаем проверку
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true;
        }
        // Проверяем длину номера телефона
        if (phoneNumber.length() != 12) {
            return false;
        }
        return Pattern.compile(PHONE_REGEX).matcher(phoneNumber).matches();
    }

    /**
     * Проверяет, является ли пароль допустимым.
     *
     * @param password Пароль для проверки.
     * @return true, если пароль допустим, иначе false.
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Проверяет, совпадают ли пароль и подтверждение пароля.
     *
     * @param password        Пароль.
     * @param confirmPassword Подтверждение пароля.
     * @return true, если пароли совпадают, иначе false.
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}