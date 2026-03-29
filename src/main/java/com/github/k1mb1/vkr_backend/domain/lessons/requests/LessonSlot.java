package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * A single "slot" within a recurring schedule cycle.
 *
 * <p>Example: in a 2-week cycle, a LECTURE on Monday of week 0 and
 * a PRACTICE on Wednesday of both weeks would be expressed as three slots:
 * <pre>
 * { "type": "LECTURE",  "weekIndex": 0, "daysOfWeek": ["MONDAY"],    "time": "09:00" }
 * { "type": "PRACTICE", "weekIndex": 0, "daysOfWeek": ["WEDNESDAY"], "time": "11:00" }
 * { "type": "PRACTICE", "weekIndex": 1, "daysOfWeek": ["WEDNESDAY"], "time": "11:00" }
 * </pre>
 *
 * <p>For a 1-week cycle {@code weekIndex} defaults to 0 and can be omitted.
 */
public record LessonSlot(

    /** LECTURE or PRACTICE — NOT NONE. */
    @NotNull LessonType type,

    /**
     * 0-based index of the week within the interval cycle in which this slot fires.
     * Must be in range [0, intervalWeeks - 1].
     * Defaults to 0 when null (valid for intervalWeeks == 1).
     */
    @Min(0) Integer weekIndex,

    /** Days of the week within the chosen week on which this slot fires. */
    @NotEmpty List<@NotNull DayOfWeek> daysOfWeek,

    /** Start time of the lesson. */
    @NotNull @JsonFormat(pattern = "HH:mm") LocalTime time,

    /**
     * Target group or subgroup for this lesson.
     * NULL = lesson is for all students enrolled in the subject (e.g. a lecture).
     * Non-null = lesson is restricted to the specified group/subgroup.
     */
    UUID groupId

) {
    /** Returns weekIndex defaulting to 0 when not provided. */
    public int resolvedWeekIndex() {
        return weekIndex == null ? 0 : weekIndex;
    }
}
