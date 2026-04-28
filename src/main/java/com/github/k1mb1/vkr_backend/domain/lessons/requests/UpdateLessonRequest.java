package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.IssuanceMode;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Partial update request for a lesson. All fields are optional — only non-null
 * values are applied.
 */
public record UpdateLessonRequest(
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID groupId,
    IssuanceMode issuanceMode,
    PenaltyMode penaltyMode,
    @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal penaltyStep
) {}
