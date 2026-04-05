package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A single recurring slot inside a {@link LessonScheduleEntry}.
 *
 * <p>One slot = one lesson per matching week. To schedule two different lesson
 * types on different days, use two separate slots.
 *
 * <p>Rules:
 * <ul>
 *   <li>{@code time} – wall-clock start time (required).</li>
 *   <li>{@code dayOfWeek} – the day this slot falls on (one value per slot,
 *       not a list — avoids ambiguity when type/group differs per day).</li>
 *   <li>{@code groupId} – must be {@code null} for {@link LessonType#LECTURE}
 *       because lectures always target the whole enrolled cohort.</li>
 * </ul>
 */
public record LessonSlot(
    @NotNull LessonType type,
    @NotNull LocalTime time,
    @Min(0) Integer weekIndex,
    @NotNull DayOfWeek dayOfWeek,
    UUID groupId
) {
    public int resolvedWeekIndex() {
        return weekIndex == null ? 0 : weekIndex;
    }

    /** Lectures are whole-cohort — {@code groupId} must not be set. */
    @AssertTrue(message = "groupId must be null for LECTURE slots")
    public boolean isGroupIdNullForLecture() {
        return type != LessonType.LECTURE || groupId == null;
    }
}
