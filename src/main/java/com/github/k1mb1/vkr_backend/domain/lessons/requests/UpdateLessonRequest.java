package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.UUID;

public record UpdateLessonRequest(
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID subjectId,
    Boolean archived,
    Instant archivedAt
) {}
