package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * A single recurring slot inside a {@link LessonScheduleEntry}.
 *
 * <p>Rules:
 * <ul>
 *   <li>{@code time} – wall-clock start time of the lesson (required).</li>
 *   <li>{@code groupId} – target group/subgroup UUID.
 *       <b>Must be {@code null} for {@link LessonType#LECTURE}</b> because
 *       lectures are always for the whole cohort enrolled in the subject.</li>
 * </ul>
 */
public record LessonSlot(
    @NotNull LessonType type,
    @NotNull LocalTime time,
    @Min(0) Integer weekIndex,
    @NotEmpty List<@NotNull DayOfWeek> daysOfWeek,
    UUID groupId
) {
    public int resolvedWeekIndex() {
        return weekIndex == null ? 0 : weekIndex;
    }

    /**
     * Lectures are for the whole group — {@code groupId} must not be set.
     * Practices may optionally target a subgroup.
     */
    @AssertTrue(message = "groupId must be null for LECTURE slots")
    public boolean isGroupIdNullForLecture() {
        return type != LessonType.LECTURE || groupId == null;
    }
}
