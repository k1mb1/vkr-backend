package com.github.k1mb1.vkr_backend.common.security;

import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Резолвит тонкие права пользователя из БД и кэширует результат под ключом {@code sub}.
 * Один поход в базу на пользователя вместо повторных проверок в каждом сервисе.
 *
 * <p>Кэш {@code userPermissions} живёт недолго (см. {@code CacheConfig}) и принудительно
 * сбрасывается при изменении прав ({@link TeacherSubjectPermissionRepository}-мутации
 * помечены {@code @CacheEvict}). Бин отдельный от вызывающих сервисов — иначе
 * self-invocation обошёл бы прокси и кэш не сработал бы.
 */
@Service
@RequiredArgsConstructor
public class PermissionResolver {

    private final TeacherSubjectPermissionRepository permissionRepository;

    @Cacheable(cacheNames = "userPermissions", key = "#teacherId")
    @Transactional(readOnly = true)
    public UserPermissions forUser(UUID teacherId) {
        Set<UUID> permissionIds = new HashSet<>();
        Set<UUID> subjectIds = new HashSet<>();
        Set<UUID> fullAccessSubjectIds = new HashSet<>();
        for (var row : permissionRepository.findOwnedByTeacherId(teacherId)) {
            permissionIds.add(row.getPermissionId());
            subjectIds.add(row.getSubjectId());
            if (row.getAllPermissions()) {
                fullAccessSubjectIds.add(row.getSubjectId());
            }
        }
        return new UserPermissions(
            Set.copyOf(permissionIds),
            Set.copyOf(subjectIds),
            Set.copyOf(fullAccessSubjectIds)
        );
    }
}
