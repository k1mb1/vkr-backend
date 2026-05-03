package com.github.k1mb1.vkr_backend.education.lessons.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Lightweight DTO used by other modules to query lesson metadata without accessing internal entities. */
public record LessonInfo(
    UUID id,
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID subjectId,
    UUID groupId,
    int issuedTaskIndex,
    PenaltyMode penaltyMode,
    BigDecimal penaltyStep
) {}
