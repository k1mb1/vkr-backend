package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;
import com.github.k1mb1.vkr_backend.domain.student_grades.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * A single grade cell: one student × one task.
 */
public record GradeSheetGradeCell(
    UUID gradeId,
    Integer value,
    SubmissionStatus status,
    Instant submittedAt
) {}
