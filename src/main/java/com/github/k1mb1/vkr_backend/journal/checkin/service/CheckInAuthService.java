package com.github.k1mb1.vkr_backend.journal.checkin.service;

import com.github.k1mb1.vkr_backend.auth.api.CheckInAuthPort;
import com.github.k1mb1.vkr_backend.journal.checkin.repository.CheckInSessionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация {@link CheckInAuthPort}: модуль attendance владеет check-in сессиями
 * и разрешает их в предметы для авторизационных проверок auth.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInAuthService implements CheckInAuthPort {

    private final CheckInSessionRepository sessionRepository;

    @Override
    public Optional<UUID> subjectIdOfCheckInSession(UUID sessionId) {
        return sessionRepository.findSubjectIdById(sessionId);
    }
}
