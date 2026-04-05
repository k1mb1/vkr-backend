package com.github.k1mb1.vkr_backend.domain.lessons;

import java.time.OffsetDateTime;
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

    /**
     * Used during bulk-schedule to detect duplicates.
     * A duplicate is defined as: same subject, same dateTime, same type, same group
     * (group may be null for lectures).
     */
    boolean existsBySubject_IdAndDateTimeAndTypeAndGroup_Id(
        UUID subjectId,
        OffsetDateTime dateTime,
        LessonType type,
        UUID groupId
    );
}
