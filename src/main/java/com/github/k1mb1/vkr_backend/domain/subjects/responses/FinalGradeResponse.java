package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import java.util.UUID;

/**
 * Aggregated final grade for one student in a subject.
 *
 * @param studentId    Student UUID.
 * @param username     Student display name.
 * @param earnedPoints Sum of (grade.value × displacementCoeff) across all tasks.
 *                     Ungraded mandatory tasks contribute 0.
 * @param maxPoints    Sum of (task.maxPoints × displacementCoeff) for mandatory tasks only.
 * @param percentage   earnedPoints / maxPoints × 100, or {@code null} when maxPoints = 0.
 */
public record FinalGradeResponse(
    UUID studentId,
    String username,
    double earnedPoints,
    double maxPoints,
    Double percentage
) {}
