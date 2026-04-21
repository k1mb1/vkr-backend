package com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.PenaltyMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request body for creating a new {@link com.github.k1mb1.vkr_backend.domain.lesson_tasks.LessonTaskEntity}.
 *
 * @param title           Short name of the task.
 * @param description     Optional detailed description.
 * @param maxPoints       Maximum achievable points (must be >= 1).
 * @param position        0-based display position within the lesson.
 * @param issuedTaskIndex Position of the currently-active (latest issued) task in the lesson.
 *                        Used by the front-end to compute displacement coefficients.
 * @param penaltyMode     How to decay superseded tasks: SUBTRACT or MULTIPLY.
 * @param penaltyStep     Step size for the decay (e.g. 0.25 for SUBTRACT, 0.5 for MULTIPLY).
 * @param isMandatory     Whether this task is mandatory (default true).
 *                        Mandatory tasks always count in the total even if not submitted (score = 0).
 *                        Bonus tasks (false) only count when submitted.
 * @param deadline        Optional submission deadline. Null = no hard deadline.
 */
public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    @Min(1) int maxPoints,
    @Min(0) int position,
    @Min(0) int issuedTaskIndex,
    @NotNull PenaltyMode penaltyMode,
    @NotNull @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal penaltyStep,
    boolean isMandatory,
    Instant deadline
) {}
