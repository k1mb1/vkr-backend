package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import java.util.List;
import java.util.UUID;

/**
 * Top-level grade-sheet response returned by
 * {@code GET /api/subjects/{subjectId}/grade-sheet}.
 *
 * <p>All data needed to render the teacher's journal table is contained in a
 * single response, avoiding N+1 round trips from the front-end.
 *
 * <p>Structure:
 * <pre>
 *   lessons (columns) ─► each lesson has tasks (sub-columns)
 *   students (rows)   ─► each student has:
 *       grades      – Map&lt;taskId, GradeSheetGradeCell&gt;
 *       attendances – Map&lt;lessonId, PresenceType&gt;
 * </pre>
 *
 * <p>The front-end uses the lesson/task metadata (decayFactor, penaltyMode,
 * penaltyStep, issuedTaskIndex, isMandatory) to compute the weighted totals
 * without additional requests.
 */
public record GradeSheetResponse(
    UUID subjectId,
    List<GradeSheetLessonResponse> lessons,
    List<GradeSheetStudentRow> students
) {}
