package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request body for creating a new {@link com.github.k1mb1.vkr_backend.domain.lessons.LessonTaskEntity}.
 *
 * @param title           Short name of the task.
 * @param description     Optional detailed description.
 * @param maxPoints       Maximum achievable points (must be >= 1).
 * @param position        0-based display position within the lesson.
 * @param issuedTaskIndex Position of the currently-active (latest issued) task in the lesson.
 *                        Used by the front-end to compute displacement coefficients.
 * @param penaltyMode     How to decay superseded tasks: SUBTRACT or MULTIPLY.
 * @param penaltyStep     Step size for the decay (e.g. 0.25 for SUBTRACT, 0.5 for MULTIPLY).
 */
public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    @Min(1) int maxPoints,
    @Min(0) int position,
    @Min(0) int issuedTaskIndex,
    @NotNull PenaltyMode penaltyMode,
    @NotNull @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal penaltyStep
) {}
