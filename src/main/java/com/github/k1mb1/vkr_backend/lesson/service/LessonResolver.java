package com.github.k1mb1.vkr_backend.lesson.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.domain.PermissionScopeEntity;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Общая логика разрешения занятий по фильтру (scope/lesson) и проверок их
 * принадлежности предмету разрешения. Вынесена из таблиц посещаемости, оценок
 * и check-in сессий, где раньше дублировалась дословно.
 */
@Component
@RequiredArgsConstructor
public class LessonResolver {

    private final LessonRepository lessonRepository;
    private final LessonScopeRepository lessonScopeRepository;

    /**
     * Разрешает множество занятий с учётом фильтра:
     * <ul>
     *   <li>{@code lessonScopeId} задан — занятие этого scope'а (с проверкой предмета и согласованности с {@code lessonId});</li>
     *   <li>иначе {@code lessonId} задан — это занятие (с проверкой предмета);</li>
     *   <li>иначе — все занятия, видимые под разрешением.</li>
     * </ul>
     */
    public List<LessonEntity> resolveLessons(
            TeacherSubjectPermissionEntity permission, UUID lessonScopeId, UUID lessonId) {
        if (lessonScopeId != null) {
            var scope = lessonScopeRepository
                    .findById(lessonScopeId)
                    .orElseThrow(() -> new ResourceNotFoundException("LessonScope", lessonScopeId));
            assertSameSubject(scope.getLesson(), permission);
            assertLessonMatch(scope.getLesson(), lessonId);
            return List.of(scope.getLesson());
        }
        if (lessonId != null) {
            var lesson = lessonRepository
                    .findById(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
            assertSameSubject(lesson, permission);
            return List.of(lesson);
        }
        return lessonRepository.findAllWithDetails(LessonSpecifications.forPermission(permission));
    }

    /** Проверяет, что занятие принадлежит предмету разрешения. */
    public void assertSameSubject(LessonEntity lesson, TeacherSubjectPermissionEntity permission) {
        if (!lesson.getSubject().getId().equals(permission.getSubject().getId())) {
            throw new IllegalArgumentException(
                    "Lesson " + lesson.getId() + " does not belong to subject of permission " + permission.getId());
        }
    }

    /** Проверяет, что scope относится к запрошенному занятию (если оно задано). */
    public void assertLessonMatch(LessonEntity scopeLesson, UUID requestedLessonId) {
        if (requestedLessonId != null && !scopeLesson.getId().equals(requestedLessonId)) {
            throw new IllegalArgumentException(
                    "lessonScopeId belongs to lesson " + scopeLesson.getId() + " but lessonId=" + requestedLessonId);
        }
    }

    /**
     * Аудитория разрешения — отсортированные scope'ы (группа, опц. подгруппа).
     * Пустой список означает «все группы предмета» (allPermissions или scope с group=null).
     * Сортировка единая для всех таблиц; маппинг в DTO модуля — на стороне вызывающего.
     */
    public List<PermissionScopeEntity> audienceScopes(TeacherSubjectPermissionEntity permission) {
        if (LessonSpecifications.permissionAllowsAllGroups(permission)) {
            return List.of();
        }
        return permission.getScopes().stream()
                .sorted(Comparator.comparing((PermissionScopeEntity s) ->
                                Objects.requireNonNull(s.getGroup()).getName())
                        .thenComparing(s -> s.getAllowedSubgroup() == null
                                ? -1
                                : s.getAllowedSubgroup().getIndex()))
                .toList();
    }
}
