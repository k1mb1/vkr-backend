package com.github.k1mb1.vkr_backend.lesson.service;

import com.github.k1mb1.vkr_backend.auth.api.LessonAuthPort;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация {@link LessonAuthPort}: модуль lesson разрешает занятия и проведения
 * в предметы для авторизационных проверок auth.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonAuthService implements LessonAuthPort {

    private final LessonRepository lessonRepository;
    private final LessonScopeRepository lessonScopeRepository;

    @Override
    public Set<UUID> subjectIdsOfLessons(Collection<UUID> lessonIds) {
        return lessonRepository.findSubjectIdsByLessonIds(lessonIds);
    }

    @Override
    public Set<UUID> subjectIdsOfLessonScopes(Collection<UUID> lessonScopeIds) {
        return lessonScopeRepository.findSubjectIdsByScopeIds(lessonScopeIds);
    }
}
