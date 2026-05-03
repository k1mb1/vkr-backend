package com.github.k1mb1.vkr_backend.education.lessons.internal;

import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LessonRepository extends JpaRepository<LessonEntity, UUID>, JpaSpecificationExecutor<LessonEntity> {
    boolean existsBySubjectIdAndDateTimeAndTypeAndGroupId(UUID subjectId, OffsetDateTime dateTime, LessonType type, UUID groupId);
    List<LessonEntity> findAllBySubjectId(UUID subjectId);
}
