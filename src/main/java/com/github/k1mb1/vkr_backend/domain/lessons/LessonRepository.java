package com.github.k1mb1.vkr_backend.domain.lessons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<LessonEntity, UUID>, JpaSpecificationExecutor<LessonEntity> {
    List<LessonEntity> findAllBySubject_Id(UUID subjectId);

    List<LessonEntity> findAllBySubject_Students_Id(UUID studentId);
}