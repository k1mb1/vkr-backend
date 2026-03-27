package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * One recurring schedule rule that can describe multiple lesson types (lectures + practices)
 * within the same interval cycle — eliminating the need for a separate entry per type.
 *
 * <p>The {@code slots} array defines what fires in each week of the cycle.
 * {@code weekIndex} inside each slot identifies which week of the cycle (0-based) it belongs to.
 *
 * <p>WEEKLY example — 2-week cycle, lecture every week on Monday, practice only on odd weeks Wednesday:
 * <pre>
 * {
 *   "recurrence": "WEEKLY",
 *   "intervalWeeks": 2,
 *   "startDate": "2025-09-01",
 *   "totalCount": 16,
 *   "slots": [
 *     { "type": "LECTURE",  "weekIndex": 0, "daysOfWeek": ["MONDAY"],    "time": "09:00" },
 *     { "type": "LECTURE",  "weekIndex": 1, "daysOfWeek": ["MONDAY"],    "time": "09:00" },
 *     { "type": "PRACTICE", "weekIndex": 1, "daysOfWeek": ["WEDNESDAY"], "time": "11:00" }
 *   ]
 * }
 * </pre>
 *
 * <p>MONTHLY example — every month, lecture on Monday and practice on Friday:
 * <pre>
 * {
 *   "recurrence": "MONTHLY",
 *   "intervalMonths": 1,
 *   "startDate": "2025-09-01",
 *   "totalCount": 10,
 *   "slots": [
 *     { "type": "LECTURE",  "daysOfWeek": ["MONDAY"],  "time": "09:00" },
 *     { "type": "PRACTICE", "daysOfWeek": ["FRIDAY"],  "time": "11:00" }
 *   ]
 * }
 * </pre>
 *
 * <p>Two lessons of the same type on the same day: add two slots with the same weekIndex and day
 * but different {@code time}. Generated names get ordinal suffixes, e.g. "Лекция 3 (2)".
 */
public record LessonScheduleEntry(

    /** Recurrence strategy. */
    @NotNull RecurrenceType recurrence,

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
     * Total number of lessons to generate across ALL slots in this entry combined.
     * Generation stops once this many lessons have been collected.
     */
    @NotNull @Min(1) Integer totalCount,

    /**
     * All lesson slots within one cycle iteration.
     * Each slot specifies the type, weekIndex (for WEEKLY), days of week, and time.
     */
    @NotEmpty List<@Valid LessonSlot> slots

) {

    @AssertTrue(message = "intervalWeeks is required for WEEKLY recurrence")
    public boolean hasIntervalWeeksIfWeekly() {
        return recurrence != RecurrenceType.WEEKLY || intervalWeeks != null;
    }

    @AssertTrue(message = "intervalMonths is required for MONTHLY recurrence")
    public boolean hasIntervalMonthsIfMonthly() {
        return recurrence != RecurrenceType.MONTHLY || intervalMonths != null;
    }

    @AssertTrue(message = "weekIndex must be in range [0, intervalWeeks-1] for all WEEKLY slots")
    public boolean hasValidWeekIndices() {
        if (recurrence != RecurrenceType.WEEKLY || intervalWeeks == null || slots == null) return true;
        return slots.stream().allMatch(s -> s.resolvedWeekIndex() < intervalWeeks);
    }
}
