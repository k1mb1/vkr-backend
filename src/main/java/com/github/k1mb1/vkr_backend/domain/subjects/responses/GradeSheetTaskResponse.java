package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Compact task descriptor used inside the grade-sheet response.
 * Contains everything the front-end needs to compute displacement coefficients.
 */
public record GradeSheetTaskResponse(
    UUID id,
    String title,
    int maxPoints,
    int position,
    int issuedTaskIndex,
    PenaltyMode penaltyMode,
    BigDecimal penaltyStep,
    boolean isMandatory,
    Instant deadline
) {}
