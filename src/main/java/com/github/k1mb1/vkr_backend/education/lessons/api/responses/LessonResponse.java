package com.github.k1mb1.vkr_backend.education.lessons.api.responses;

import com.github.k1mb1.vkr_backend.education.lessons.api.IssuanceMode;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import com.github.k1mb1.vkr_backend.education.lessons.api.PenaltyMode;
import java.math.BigDecimal;
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
    Integer subgroupNumber,
    IssuanceMode issuanceMode,
    Instant issuedAt,
    int issuedTaskIndex,
    PenaltyMode penaltyMode,
    BigDecimal penaltyStep,
    Instant createdAt,
    Instant updatedAt
) {}
