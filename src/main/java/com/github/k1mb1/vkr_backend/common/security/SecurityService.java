package com.github.k1mb1.vkr_backend.common.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Тонкая обёртка над {@link SecurityContextHolder}: достаёт идентичность текущего
 * пользователя из JWT. Identity берётся ТОЛЬКО из токена ({@code sub}), никогда из
 * параметров запроса — это и есть стабильный id, по которому резолвятся права в БД.
 */
@Component("securityService")
public class SecurityService {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public boolean isSameUser(UUID id) {
        return id != null && currentSubjectId().map(id::equals).orElse(false);
    }

    /**
     * {@code sub} текущего пользователя как UUID. Пусто, если запрос не аутентифицирован
     * или принципал — не JWT (например, публичные check-in эндпоинты).
     */
    public Optional<UUID> currentSubjectId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(jwt.getSubject()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isAdmin() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }
}
