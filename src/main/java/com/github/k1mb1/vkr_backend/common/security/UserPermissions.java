package com.github.k1mb1.vkr_backend.common.security;

import java.util.Set;
import java.util.UUID;

/**
 * Иммутабельный снапшот тонких прав одного пользователя, посчитанный из БД за один
 * запрос и кэшируемый на короткое время (см. {@link PermissionResolver}). Содержит
 * только то, что нужно для проверок «можно/нельзя»: id выданных ему permission'ов,
 * id предметов, к которым он допущен, и id предметов, на которые у него полный доступ
 * ({@code allPermissions=true}) — именно они дают право управлять предметом. Сами scope'ы
 * (группы/подгруппы) для фильтрации данных остаются в доменных сервисах — это не дело
 * авторизации.
 */
public record UserPermissions(
    Set<UUID> permissionIds,
    Set<UUID> subjectIds,
    Set<UUID> fullAccessSubjectIds
) {

    /** Совместимый конструктор для случаев без явного набора «полных» предметов. */
    public UserPermissions(Set<UUID> permissionIds, Set<UUID> subjectIds) {
        this(permissionIds, subjectIds, Set.of());
    }

    public boolean ownsPermission(UUID permissionId) {
        return permissionId != null && permissionIds.contains(permissionId);
    }

    public boolean hasSubject(UUID subjectId) {
        return subjectId != null && subjectIds.contains(subjectId);
    }

    /** Полный доступ ({@code allPermissions=true}) к предмету — право им управлять. */
    public boolean hasFullAccessToSubject(UUID subjectId) {
        return subjectId != null && fullAccessSubjectIds.contains(subjectId);
    }
}
