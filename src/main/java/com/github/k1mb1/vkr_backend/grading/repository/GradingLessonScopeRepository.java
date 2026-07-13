package com.github.k1mb1.vkr_backend.grading.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

/** Read-only доступ модуля grading к проведениям занятий (lesson::domain). */
public interface GradingLessonScopeRepository extends Repository<LessonScopeEntity, UUID> {

    @EntityGraph(attributePaths = {"lesson", "lesson.subject", "group", "allowedSubgroup"})
    Optional<LessonScopeEntity> findWithDetailsById(UUID id);
}
