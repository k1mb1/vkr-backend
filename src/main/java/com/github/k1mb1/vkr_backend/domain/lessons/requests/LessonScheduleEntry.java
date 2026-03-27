package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Describes a recurring schedule for one lesson type (LECTURE or PRACTICE).
 *
 * <p>Recurrence modes:
 * <ul>
 *   <li>{@code WEEKLY}  — repeat every {@code intervalWeeks} weeks on the given {@code daysOfWeek}.</li>
 *   <li>{@code MONTHLY} — repeat every {@code intervalMonths} months on the given {@code daysOfWeek}
 *       (first matching weekday on or after the anchor day of the month).</li>
 * </ul>
 *
 * <p>Stop condition: supply either {@code endDate} or {@code totalCount}, not both.
 *
 * <p>Two lessons on the same day: add two entries with the same day but different {@code time} values.
 * The generated lesson name will include an ordinal suffix when multiple lessons land on the same date
 * (e.g. "Лекция 1 (2)" for the second lecture of that day).
 */
public record LessonScheduleEntry(

    /** LECTURE or PRACTICE — NOT NONE. */
    @NotNull LessonType type,

    /** Recurrence strategy. */
    @NotNull RecurrenceType recurrence,

    /**
     * Days of week on which this lesson occurs.
     * For WEEKLY: every intervalWeeks-th week on these days.
     * For MONTHLY: the first occurrence of each listed weekday on/after the anchor date each month.
     */
    @NotEmpty List<@NotNull DayOfWeek> daysOfWeek,

    /**
     * Start time of the lesson (e.g. 08:30).
     * When two entries share the same date and type, ordinal suffixes are added to names
     * so they remain distinguishable.
     */
    @NotNull @JsonFormat(pattern = "HH:mm") LocalTime time,

    /** Date of the very first occurrence; the schedule begins from this date. */
    @NotNull LocalDate startDate,

    /**
     * Repeat every N weeks (only used when recurrence == WEEKLY).
     * 1 = every week, 2 = every other week, etc.
     */
    @Min(1) Integer intervalWeeks,

    /**
     * Repeat every N months (only used when recurrence == MONTHLY).
     * 1 = every month, 2 = every other month, etc.
     */
    @Min(1) Integer intervalMonths,

    /**
     * Inclusive end date — generate lessons up to and including this date.
     * Supply either {@code endDate} or {@code totalCount}, not both.
     */
    LocalDate endDate,

    /**
     * Maximum number of lessons to generate for this entry.
     * Supply either {@code endDate} or {@code totalCount}, not both.
     */
    @Min(1) Integer totalCount
) {

    @AssertTrue(message = "Provide either endDate or totalCount, not both and not neither")
    public boolean hasExactlyOneStopCondition() {
        return (endDate != null) ^ (totalCount != null);
    }

    @AssertTrue(message = "type must be LECTURE or PRACTICE, not NONE")
    public boolean hasConcreteType() {
        return type != null && type != LessonType.NONE;
    }

    @AssertTrue(message = "intervalWeeks is required for WEEKLY recurrence")
    public boolean hasIntervalWeeksIfWeekly() {
        return recurrence != RecurrenceType.WEEKLY || intervalWeeks != null;
    }

    @AssertTrue(message = "intervalMonths is required for MONTHLY recurrence")
    public boolean hasIntervalMonthsIfMonthly() {
        return recurrence != RecurrenceType.MONTHLY || intervalMonths != null;
    }
}
