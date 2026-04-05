package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Compact lesson descriptor used inside the grade-sheet response.
 */
public record GradeSheetLessonResponse(
    UUID id,
    String name,
    OffsetDateTime dateTime,
    LessonType type,
    UUID groupId,
    Integer subgroupNumber,
    BigDecimal decayFactor,
    List<GradeSheetTaskResponse> tasks
) {}
