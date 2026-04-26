package com.github.k1mb1.vkr_backend.domain.lesson_tasks.responses;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for a lesson task.
 *
 * <p>Displacement penalty configuration (penaltyMode, penaltyStep, issuedTaskIndex)
 * is stored on the parent lesson, not on individual tasks. Retrieve it from
 * {@code LessonResponse} and apply the formula per task:
 * <pre>
 *   d = lesson.issuedTaskIndex - task.position
 *   NONE:     coeff = 1.0
 *   SUBTRACT: coeff = max(0, 1 - lesson.penaltyStep * d)
 *   MULTIPLY: coeff = lesson.penaltyStep ^ d
 * </pre>
 */
public record TaskResponse(
    UUID id,
    UUID lessonId,
    String title,
    String description,
    int maxPoints,
    int position,
    boolean isMandatory,
    Instant deadline,
    Instant createdAt,
    Instant updatedAt
) {}
