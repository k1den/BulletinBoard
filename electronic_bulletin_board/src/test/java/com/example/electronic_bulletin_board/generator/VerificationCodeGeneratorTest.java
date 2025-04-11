package com.example.electronic_bulletin_board.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class VerificationCodeGeneratorTest {

    @Test
    public void testGenerateVerificationCode() {
        String code = VerificationCodeGenerator.generateVerificationCode();
        assertNotNull(code);
        assertEquals(6, code.length());
    }
}