package com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests;

import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * Request body for creating a new task in a lesson.
 *
 * @param title       Short name of the task.
 * @param description Optional detailed description.
 * @param maxPoints   Maximum achievable points (must be >= 1).
 * @param position    0-based display position within the lesson.
 * @param isMandatory Whether this task is mandatory (default true).
 *                    Mandatory tasks always count in the total even if not submitted (score = 0).
 *                    Bonus tasks (false) only count when submitted.
 * @param deadline    Optional submission deadline. Null = no hard deadline.
 */
public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    @Min(1) int maxPoints,
    @Min(0) int position,
    boolean isMandatory,
    Instant deadline
) {}
