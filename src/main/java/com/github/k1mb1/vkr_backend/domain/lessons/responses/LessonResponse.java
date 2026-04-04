package com.github.k1mb1.vkr_backend.domain.lessons.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
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
 */
public record LessonResponse(
    UUID id,
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID subjectId,
    UUID groupId,
    Integer subgroupNumber,
    /**
     * Decay coefficient for this lesson [0..1]. 1.0 = no decay.
     * Front-end multiplies the summed weighted task scores by this value.
     */
    BigDecimal decayFactor,
    boolean archived,
    Instant archivedAt,
    Instant createdAt,
    Instant updatedAt
) {}
