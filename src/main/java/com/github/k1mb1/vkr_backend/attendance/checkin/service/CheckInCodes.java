package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Сверка кода аудитории check-in. Нормализует регистр/пробелы и сравнивает за постоянное время,
 * чтобы по времени ответа нельзя было подбирать код посимвольно.
 */
final class CheckInCodes {

    private CheckInCodes() {}

    static boolean matches(String expected, String provided) {
        if (expected == null || provided == null) {
            return false;
        }
        var a = expected.trim().toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        var b = provided.trim().toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
