package com.github.k1mb1.vkr_backend.education.lessons.api.requests;

import com.github.k1mb1.vkr_backend.education.lessons.api.IssuanceMode;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import com.github.k1mb1.vkr_backend.education.lessons.api.PenaltyMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateLessonRequest(
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID groupId,
    IssuanceMode issuanceMode,
    PenaltyMode penaltyMode,
    @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal penaltyStep
) {}
