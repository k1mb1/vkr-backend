package com.github.k1mb1.vkr_backend.subject.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;

import java.time.Instant;
import java.util.UUID;

public record SubjectAssignmentResponse(
    UUID id,

    UUID teacherId,

    UUID offeringId,

    UUID subgroupId,

    LessonType lessonTypeScope,

    Instant createdAt,

    Instant updatedAt
) {}