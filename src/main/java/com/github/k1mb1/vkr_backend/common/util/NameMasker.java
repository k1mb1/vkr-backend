package com.github.k1mb1.vkr_backend.common.util;

/**
 * Маскирование (псевдонимизация) ФИО для публичного показа.
 * <p>
 * Применяется там, где список студентов доступен без аутентификации
 * (например, публичная страница check-in по QR-коду), чтобы посторонний
 * не мог собрать полную базу ФИО. Студент по-прежнему узнаёт себя в списке,
 * а идентификация при отметке идёт по id, а не по имени, поэтому
 * маскирование не мешает самому процессу check-in.
 */
public final class NameMasker {

    private NameMasker() {}

    /**
     * Сокращает ФИО до фамилии и инициалов в стиле учебной ведомости.
     * <ul>
     *     <li>«Иванов Иван Иванович» → «Иванов И. И.»</li>
     *     <li>«Петров Пётр» → «Петров П.»</li>
     *     <li>«Сидоров» → «Сидоров» (нечего сокращать)</li>
     * </ul>
     * Значения null/blank возвращаются без изменений.
     */
    public static String maskFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return fullName;
        }

        var parts = fullName.trim().split("\\s+", -1);
        var masked = new StringBuilder(parts[0]);
        for (var i = 1; i < parts.length; i++) {
            var part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            masked.append(' ').append(Character.toUpperCase(part.charAt(0))).append('.');
        }
        return masked.toString();
    }
}
