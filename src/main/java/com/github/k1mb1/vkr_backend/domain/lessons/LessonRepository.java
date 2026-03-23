package com.github.k1mb1.vkr_backend.domain.lessons;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LessonRepository
    extends
        JpaRepository<LessonEntity, UUID>,
        JpaSpecificationExecutor<LessonEntity>
{
    List<LessonEntity> findAllBySubject_Id(UUID subjectId);
}
