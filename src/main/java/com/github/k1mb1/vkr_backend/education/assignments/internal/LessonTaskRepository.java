package com.github.k1mb1.vkr_backend.education.assignments.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LessonTaskRepository extends JpaRepository<LessonTaskEntity, UUID>, JpaSpecificationExecutor<LessonTaskEntity> {
    List<LessonTaskEntity> findAllByLessonIdOrderByPositionAsc(UUID lessonId);
    Optional<LessonTaskEntity> findByIdAndLessonId(UUID id, UUID lessonId);
    List<LessonTaskEntity> findAllByLessonIdIn(Collection<UUID> lessonIds);
}
