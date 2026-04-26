package com.github.k1mb1.vkr_backend.domain.lessons.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.IssuanceMode;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import com.github.k1mb1.vkr_backend.domain.lessons.PenaltyMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lesson response DTO.
 *
 * <p>{@code groupId} — UUID of the target group/subgroup, or {@code null} for
 * whole-cohort lessons (lectures).
 *
 * <p>{@code subgroupNumber} — ordinal number of the subgroup extracted from the
 * group name (e.g. "ИСТ-21/2" → 2), or {@code null} when the lesson targets
 * the whole cohort or a main group without subgroup numbering.
 *
 * <p>Displacement penalty formula (computed by the front-end for each task):
 * <pre>
 *   d = issuedTaskIndex - task.position
 *   NONE:     coeff = 1.0
 *   SUBTRACT: coeff = max(0, 1 - penaltyStep * d)
 *   MULTIPLY: coeff = penaltyStep ^ d
 * </pre>
 */
public record LessonResponse(
    UUID id,
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID subjectId,
    UUID groupId,
    Integer subgroupNumber,
    IssuanceMode issuanceMode,
    Instant issuedAt,
    int issuedTaskIndex,
    PenaltyMode penaltyMode,
    BigDecimal penaltyStep,
    boolean archived,
    Instant archivedAt,
    Instant createdAt,
    Instant updatedAt
) {}
