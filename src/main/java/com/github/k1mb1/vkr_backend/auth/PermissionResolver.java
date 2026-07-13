package com.github.k1mb1.vkr_backend.auth;

import com.github.k1mb1.vkr_backend.auth.api.PermissionAuthPort;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Резолвит тонкие права пользователя из БД и кэширует результат под ключом {@code sub}.
 * Один поход в базу на пользователя вместо повторных проверок в каждом сервисе.
 * Данные о правах поступают через {@link PermissionAuthPort}, реализуемый модулем
 * subject, — auth не зависит от бизнес-модулей.
 *
 * <p>Кэш {@code userPermissions} живёт недолго (см. {@code CacheConfig}) и принудительно
 * сбрасывается при изменении прав (мутации прав в модуле subject
 * помечены {@code @CacheEvict}). Бин отдельный от вызывающих сервисов — иначе
 * self-invocation обошёл бы прокси и кэш не сработал бы.
 */
@Component
@RequiredArgsConstructor
public class PermissionResolver {

    private final PermissionAuthPort permissionAuthPort;

    @Cacheable(cacheNames = "userPermissions", key = "#teacherId")
    public UserPermissions forUser(UUID teacherId) {
        Set<UUID> permissionIds = new HashSet<>();
        Set<UUID> subjectIds = new HashSet<>();
        Set<UUID> fullAccessSubjectIds = new HashSet<>();
        for (var grant : permissionAuthPort.grantsOfTeacher(teacherId)) {
            permissionIds.add(grant.permissionId());
            subjectIds.add(grant.subjectId());
            if (grant.allPermissions()) {
                fullAccessSubjectIds.add(grant.subjectId());
            }
        }
        return new UserPermissions(Set.copyOf(permissionIds), Set.copyOf(subjectIds), Set.copyOf(fullAccessSubjectIds));
    }
}
