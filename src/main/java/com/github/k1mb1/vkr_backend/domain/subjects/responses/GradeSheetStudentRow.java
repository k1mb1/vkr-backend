package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;
import java.util.Map;
import java.util.UUID;

/**
 * One row of the grade-sheet table: a student with all their grades and attendances.
 *
 * <p>{@code grades} – keyed by {@code taskId}, contains the grade cell (value, status, submittedAt).
 * A missing key means the task has not been issued to this student yet (no row in the DB).
 *
 * <p>{@code attendances} – keyed by {@code lessonId}, contains the PresenceType.
 * A missing key means attendance has not been recorded for that lesson.
 */
public record GradeSheetStudentRow(
    UUID studentId,
    String username,
    UUID groupId,
    Map<UUID, GradeSheetGradeCell> grades,
    Map<UUID, PresenceType> attendances
) {}
