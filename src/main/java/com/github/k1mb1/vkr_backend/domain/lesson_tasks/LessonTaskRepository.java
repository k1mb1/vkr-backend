package com.github.k1mb1.vkr_backend.domain.lesson_tasks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonTaskRepository extends JpaRepository<LessonTaskEntity, UUID> {

    List<LessonTaskEntity> findAllByLesson_IdOrderByPositionAsc(UUID lessonId);

    Optional<LessonTaskEntity> findByIdAndLesson_Id(UUID id, UUID lessonId);

    @Query("""
        SELECT t FROM LessonTaskEntity t
        JOIN FETCH t.lesson l
        WHERE l.subject.id = :subjectId
    """)
    List<LessonTaskEntity> findAllBySubjectId(@Param("subjectId") UUID subjectId);
}
