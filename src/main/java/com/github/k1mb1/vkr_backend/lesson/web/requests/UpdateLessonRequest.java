package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;

import java.time.Instant;
import java.util.UUID;

public record UpdateLessonRequest(
    LessonType type,
    Instant startedAt,
    Instant endedAt,
    String topic,
    UUID teacherId,
    UUID subgroupId
) {}
