package com.github.k1mb1.vkr_backend.domain.lessons;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonTaskRepository
    extends JpaRepository<LessonTaskEntity, UUID>
{
    List<LessonTaskEntity> findAllByLesson_IdOrderByPositionAsc(UUID lessonId);
}
