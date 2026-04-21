package com.github.k1mb1.vkr_backend.domain.student_grades.requests;

import com.github.k1mb1.vkr_backend.domain.student_grades.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Request body for upserting a student's grade on a specific task.
 *
 * @param studentId   Target student UUID.
 * @param value       Points awarded (null = grade not yet set, meaningful only with GRADED status).
 * @param comment     Optional teacher comment.
 * @param status      New submission lifecycle status.
 *                    The service enforces valid transitions and auto-sets
 *                    {@code submittedAt} when transitioning to SUBMITTED.
 * @param submittedAt Explicit submission timestamp override.
 *                    When null and status is SUBMITTED, the service sets it to now().
 */
public record UpsertTaskGradeRequest(
    UUID studentId,
    Integer value,
    String comment,
    SubmissionStatus status,
    Instant submittedAt
) {}
