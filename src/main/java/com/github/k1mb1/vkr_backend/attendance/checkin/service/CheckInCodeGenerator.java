package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import java.security.SecureRandom;

/**
 * Генератор кода аудитории для check-in сессии.
 * <p>
 * Алфавит без визуально неоднозначных символов (нет 0/O, 1/I/L), чтобы код было
 * легко продиктовать и набрать. Длина 6 — компромисс между удобством и стойкостью
 * к подбору на коротком окне действия сессии.
 */
final class CheckInCodeGenerator {

    private static final char[] ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();

    private static final int CODE_LENGTH = 6;

    private static final SecureRandom RANDOM = new SecureRandom();

    private CheckInCodeGenerator() {}

    static String generate() {
        var sb = new StringBuilder(CODE_LENGTH);
        for (var i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
