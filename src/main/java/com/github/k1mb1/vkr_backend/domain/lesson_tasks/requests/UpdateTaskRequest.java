package com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.PenaltyMode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request body for updating an existing task.
 * All fields are optional — only non-null values are applied.
 *
 * @param title           New title (leave null to keep existing).
 * @param description     New description (leave null to keep existing).
 * @param maxPoints       New max points.
 * @param position        New display position.
 * @param issuedTaskIndex Updated index of the currently-active task in the lesson.
 *                        The teacher updates this when issuing a newer task so the
 *                        front-end can recompute displacement for older tasks.
 * @param penaltyMode     Updated penalty mode.
 * @param penaltyStep     Updated penalty step.
 * @param isMandatory     Toggle mandatory/bonus status (null = keep existing).
 * @param deadline        New deadline (null = keep existing; use explicit "clear" endpoint to remove).
 */
public record UpdateTaskRequest(
    String title,
    String description,
    @Min(1) Integer maxPoints,
    @Min(0) Integer position,
    @Min(0) Integer issuedTaskIndex,
    PenaltyMode penaltyMode,
    @DecimalMin("0.0001") @DecimalMax("1.0") BigDecimal penaltyStep,
    Boolean isMandatory,
    Instant deadline
) {}
