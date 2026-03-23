package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import java.time.Instant;
import java.util.UUID;

public record UpdateLessonRequest(
    String name,
    Instant dateTime,
    LessonType type,
    UUID subjectId
) {}
