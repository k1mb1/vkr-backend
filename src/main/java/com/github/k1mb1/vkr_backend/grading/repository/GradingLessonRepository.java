package com.github.k1mb1.vkr_backend.grading.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.subject.LessonType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Read-only доступ модуля grading к занятиям (lesson::domain) для таблиц оценок и заданий. */
public interface GradingLessonRepository
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
}
