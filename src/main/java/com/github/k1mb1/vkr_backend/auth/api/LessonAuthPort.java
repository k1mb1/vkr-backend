package com.github.k1mb1.vkr_backend.auth.api;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * SPI авторизации, реализуемый модулем {@code lesson}: разрешает занятия и их
 * проведения (scope) в предметы для проверки доступа по подмножеству предметов.
 */
public interface LessonAuthPort {

    Set<UUID> subjectIdsOfLessons(Collection<UUID> lessonIds);

    Set<UUID> subjectIdsOfLessonScopes(Collection<UUID> lessonScopeIds);
}
