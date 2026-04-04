package com.github.k1mb1.vkr_backend.domain.lessons.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a lesson task.
 *
 * <p>Front-end displacement formula for task at {@code position = k}
 * given {@code issuedTaskIndex = n}, {@code penaltyStep}, {@code penaltyMode}:
 * <pre>
 *   d = n - k
 *   SUBTRACT: coeff = max(0, 1 - penaltyStep * d)
 *   MULTIPLY: coeff = penaltyStep ^ d
 * </pre>
 *
 * @param id              Task UUID.
 * @param lessonId        Parent lesson UUID.
 * @param title           Short task name.
 * @param description     Optional detail text.
 * @param maxPoints       Maximum achievable score.
 * @param position        0-based display order within the lesson.
 * @param issuedTaskIndex Position of the currently-active task in the lesson.
 * @param penaltyMode     SUBTRACT or MULTIPLY.
 * @param penaltyStep     Step size for the penalty (e.g. 0.25 or 0.5).
 * @param isMandatory     True = always counted in total; false = bonus, counted only if submitted.
 * @param deadline        Optional submission deadline (null = no deadline).
 * @param createdAt       Creation timestamp.
 * @param updatedAt       Last-update timestamp.
 */
public record TaskResponse(
    UUID id,
    UUID lessonId,
    String title,
    String description,
    int maxPoints,
    int position,
    int issuedTaskIndex,
    PenaltyMode penaltyMode,
    BigDecimal penaltyStep,
    boolean isMandatory,
    Instant deadline,
    Instant createdAt,
    Instant updatedAt
) {}
