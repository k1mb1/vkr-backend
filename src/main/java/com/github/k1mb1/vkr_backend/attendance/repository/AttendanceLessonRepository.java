package com.github.k1mb1.vkr_backend.attendance.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

/** Read-only доступ модуля attendance к занятиям (lesson::domain) для разрешения таблиц. */
public interface AttendanceLessonRepository
        extends Repository<LessonEntity, UUID>, JpaSpecificationExecutor<LessonEntity> {

    Optional<LessonEntity> findById(UUID id);

    @Override
    @EntityGraph("Lesson.withDetails")
    List<LessonEntity> findAll(Specification<LessonEntity> spec);
}
