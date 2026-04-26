package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import com.github.k1mb1.vkr_backend.domain.student_grades.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Grade row for a single student on a single task.
 *
 * @param id          Grade UUID.
 * @param taskId      Task UUID.
 * @param studentId   Student UUID.
 * @param value       Points awarded (null = not graded yet).
 * @param comment     Optional teacher comment.
 * @param status      Current submission lifecycle status.
 * @param submittedAt When the work was submitted (null = pending).
 * @param createdAt   Record creation time.
 * @param updatedAt   Record last-update time.
 */
public record TaskGradeResponse(
    UUID id,
    UUID taskId,
    UUID lessonId,
    UUID studentId,
    Integer value,
    String comment,
    SubmissionStatus status,
    Instant submittedAt,
    Instant createdAt,
    Instant updatedAt
) {}
