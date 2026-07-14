package com.github.k1mb1.vkr_backend.auth.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SPI авторизации, реализуемый {@code teacher}: снабжает auth данными о выданных
 * правах, не давая auth зависеть от бизнес-пакетов (инверсия зависимостей —
 * auth остаётся листом графа, реализация живёт у владельца данных).
 */
public interface PermissionAuthPort {

    /** Все права одного преподавателя одним запросом (для кэшируемого снапшота). */
    List<PermissionGrantResponse> grantsOfTeacher(UUID teacherId);

    /** Предмет, к которому относится выданное право, — для проверок управления по id права. */
    Optional<UUID> subjectIdOfPermission(UUID permissionId);
}
