package com.github.k1mb1.vkr_backend.domain.lessons;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository
    extends
        JpaRepository<LessonEntity, UUID>,
        JpaSpecificationExecutor<LessonEntity>
{
    /**
     * Used during bulk-schedule to detect duplicates.
     */
    boolean existsBySubject_IdAndDateTimeAndTypeAndGroup_Id(
        UUID subjectId,
        OffsetDateTime dateTime,
        LessonType type,
        UUID groupId
    );

    /**
     * Loads all lessons for a subject together with their tasks in a single
     * query — used by the grade-sheet endpoint to avoid N+1.
     */
    @Query("""
        SELECT DISTINCT l FROM LessonEntity l
        LEFT JOIN FETCH l.tasks
        WHERE l.subject.id = :subjectId
        ORDER BY l.dateTime ASC NULLS LAST
    """)
    List<LessonEntity> findAllWithTasksBySubjectId(@Param("subjectId") UUID subjectId);
}
