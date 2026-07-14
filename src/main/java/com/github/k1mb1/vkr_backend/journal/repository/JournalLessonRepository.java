package com.github.k1mb1.vkr_backend.journal.repository;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.LessonType;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Read-only доступ модуля journal к занятиям (lesson::domain) — общий для таблиц
 * посещаемости, оценок и check-in сессий.
 */
public interface JournalLessonRepository
        extends Repository<LessonEntity, UUID>, JpaSpecificationExecutor<LessonEntity> {

    LessonEntity getReferenceById(UUID id);

    Optional<LessonEntity> findById(UUID id);

    @Override
    @EntityGraph("Lesson.withDetails")
    List<LessonEntity> findAll(Specification<LessonEntity> spec);

    Optional<LessonEntity> findBySubjectIdAndTypeAndActiveTrue(UUID subjectId, LessonType type);

    /** Занятия предмета и типа, у которых есть задания, — колонки таблицы оценок. */
    @Query("""
        SELECT l FROM LessonEntity l
        JOIN FETCH l.subject
        WHERE l.subject.id = :subjectId AND l.type = :type
          AND EXISTS (SELECT 1 FROM AssignmentEntity a WHERE a.lesson.id = l.id)
        ORDER BY l.orderIndex
        """)
    List<LessonEntity> findWithAssignmentsBySubjectIdAndType(
            @Param("subjectId") UUID subjectId, @Param("type") LessonType type);

    /**
     * Разрешение занятий по фильтру — единая точка для всех таблиц журнала:
     * scope задан — занятие этого scope (с проверкой предмета и согласованности
     * с lessonId); иначе lessonId — это занятие; иначе все видимые под правом.
     */
    default List<LessonEntity> resolveLessons(
            JournalLessonScopeRepository scopeRepository,
            TeacherSubjectPermissionEntity permission,
            @Nullable UUID lessonScopeId,
            @Nullable UUID lessonId) {
        if (lessonScopeId != null) {
            var scope = scopeRepository
                    .findWithDetailsById(lessonScopeId)
                    .orElseThrow(() -> new ResourceNotFoundException("LessonScope", lessonScopeId));
            LessonSpecifications.assertSameSubject(scope.getLesson(), permission);
            LessonSpecifications.assertLessonMatch(scope.getLesson(), lessonId);
            return List.of(scope.getLesson());
        }
        if (lessonId != null) {
            var lesson = findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
            LessonSpecifications.assertSameSubject(lesson, permission);
            return List.of(lesson);
        }
        return findAll(LessonSpecifications.forPermission(permission));
    }
}
