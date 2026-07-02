package com.github.k1mb1.vkr_backend.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * Иммутабельный снапшот тонких прав одного пользователя, посчитанный из БД за один
 * запрос и кэшируемый на короткое время (см. {@link PermissionResolver}). Содержит
 * только то, что нужно для проверок «можно/нельзя»: id выданных ему permission'ов и
 * id предметов, к которым он допущен. Сами scope'ы (группы/подгруппы) для фильтрации
 * данных остаются в доменных сервисах — это не дело авторизации.
 */
public record UserPermissions(Set<UUID> permissionIds, Set<UUID> subjectIds) {

    public boolean ownsPermission(UUID permissionId) {
        return permissionId != null && permissionIds.contains(permissionId);
    }

    public boolean hasSubject(UUID subjectId) {
        return subjectId != null && subjectIds.contains(subjectId);
    }
}
