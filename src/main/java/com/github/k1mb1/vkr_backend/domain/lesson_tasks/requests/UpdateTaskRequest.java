package com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests;

import jakarta.validation.constraints.*;
import java.time.Instant;

/**
 * Request body for updating an existing task.
 * All fields are optional — only non-null values are applied.
 *
 * @param title       New title (leave null to keep existing).
 * @param description New description (leave null to keep existing).
 * @param maxPoints   New max points.
 * @param position    New display position.
 * @param isMandatory Toggle mandatory/bonus status (null = keep existing).
 * @param deadline    New deadline (null = keep existing).
 */
public record UpdateTaskRequest(
    String title,
    String description,
    @Min(1) Integer maxPoints,
    @Min(0) Integer position,
    Boolean isMandatory,
    Instant deadline
) {}
