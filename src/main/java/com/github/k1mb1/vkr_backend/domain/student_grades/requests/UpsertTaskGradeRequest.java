package com.github.k1mb1.vkr_backend.domain.student_grades.requests;

import java.time.Instant;
import java.util.UUID;

/**
 * Request body for upserting a student's grade on a specific task.
 *
 * @param studentId   Target student UUID.
 * @param value       Points awarded (null = grade not yet set).
 * @param comment     Optional teacher comment.
 * @param submittedAt When the student submitted their work (null = not submitted).
 */
public record UpsertTaskGradeRequest(
    UUID studentId,
    Integer value,
    String comment,
    Instant submittedAt
) {}
