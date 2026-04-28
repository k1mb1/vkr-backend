package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.IssuanceMode;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateLessonRequest(
    @NotBlank String name,
    OffsetDateTime dateTime,
    @NotNull LessonType type,
    @NotNull UUID subjectId,
    /** Null → the lesson has no group (whole-cohort / lecture). */
    UUID groupId,
    /** Null → defaults to AUTO in the entity. */
    IssuanceMode issuanceMode,
    /** Null → defaults to NONE (no penalty) in the entity. */
    PenaltyMode penaltyMode,
    /** Null → defaults to 0.25 in the entity. Ignored when penaltyMode is NONE. */
    @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal penaltyStep
) {}
