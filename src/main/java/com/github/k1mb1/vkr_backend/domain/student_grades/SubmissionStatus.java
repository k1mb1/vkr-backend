package com.github.k1mb1.vkr_backend.domain.student_grades;

/**
 * Lifecycle status of a student's submission for a {@link StudentTaskGradeEntity}.
 *
 * <pre>
 *   NOT_SUBMITTED  — task issued, student has not submitted anything yet.
 *   SUBMITTED      — student submitted work, waiting for teacher review.
 *   GRADED         — teacher reviewed and set a numeric {@code value}.
 *   RESUBMIT       — teacher returned the work for revision.
 * </pre>
 *
 * <p>State machine (typical flow):
 * <pre>
 *   NOT_SUBMITTED → SUBMITTED → GRADED
 *                            → RESUBMIT → SUBMITTED → GRADED
 * </pre>
 */
public enum SubmissionStatus {
    NOT_SUBMITTED,
    SUBMITTED,
    GRADED,
    RESUBMIT
}
