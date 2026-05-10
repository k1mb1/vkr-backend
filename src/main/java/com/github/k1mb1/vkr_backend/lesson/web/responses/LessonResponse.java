package com.github.k1mb1.vkr_backend.lesson.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;

import java.time.Instant;
import java.util.UUID;

public record LessonResponse(
    UUID id,
    UUID offeringId,
    UUID subjectId,
    UUID groupId,
    UUID subgroupId,
    LessonType type,
    UUID teacherId,
    Instant startedAt,
    Instant endedAt,
    String topic,
    Instant createdAt,
    Instant updatedAt
) {}
