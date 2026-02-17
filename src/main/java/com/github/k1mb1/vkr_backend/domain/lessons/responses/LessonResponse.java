package com.github.k1mb1.vkr_backend.domain.lessons.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;

import java.time.Instant;
import java.util.UUID;

public record LessonResponse(
        UUID id,
        String name,
        Instant dateTime,
        LessonType type,
        UUID subjectId,
        Instant createdAt,
        Instant updatedAt
) {
}