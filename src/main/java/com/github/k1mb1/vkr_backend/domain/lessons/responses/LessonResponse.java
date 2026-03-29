package com.github.k1mb1.vkr_backend.domain.lessons.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LessonResponse(
    UUID id,
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID subjectId,
    UUID groupId,
    boolean archived,
    Instant archivedAt,
    Instant createdAt,
    Instant updatedAt
) {}
