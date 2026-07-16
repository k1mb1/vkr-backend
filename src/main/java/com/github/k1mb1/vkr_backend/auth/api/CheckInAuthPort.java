package com.github.k1mb1.vkr_backend.auth.api;

import java.util.Optional;
import java.util.UUID;

/**
 * SPI авторизации, реализуемый модулем {@code attendance} (check-in): предмет,
 * к которому относится check-in сессия, — для проверки доступа к сессии по её id.
 */
public interface CheckInAuthPort {

    Optional<UUID> subjectIdOfCheckInSession(UUID sessionId);
}
