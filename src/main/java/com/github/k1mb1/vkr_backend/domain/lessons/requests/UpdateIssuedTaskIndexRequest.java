package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code PATCH /api/lessons/{id}/issued-task-index}.
 *
 * @param issuedTaskIndex Position of the task the teacher is currently issuing.
 *                        Tasks with {@code position < issuedTaskIndex} are considered
 *                        superseded and receive a displacement penalty on the front-end.
 */
public record UpdateIssuedTaskIndexRequest(
    @NotNull @Min(0) Integer issuedTaskIndex
) {}
