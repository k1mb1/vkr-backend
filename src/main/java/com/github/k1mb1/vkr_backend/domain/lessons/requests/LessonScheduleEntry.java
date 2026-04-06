package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * One scheduling block inside a {@link BulkScheduleLessonsRequest}.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code recurrence} – {@code WEEKLY} for repeating lessons, {@code ONCE} for a one-off.</li>
 *   <li>{@code startDate} – anchor date used only for WEEKLY to determine the first cycle week.
 *       For ONCE this field is ignored — each slot carries its own exact {@code date}.</li>
 *   <li>{@code intervalWeeks} – cycle length for WEEKLY (1 = every week, 2 = alternate weeks).
 *       Required when recurrence is WEEKLY.</li>
 *   <li>{@code totalCount} – how many lessons to generate in total across all slots.
 *       The teacher knows the semester count; the algorithm stops as soon as this is reached.</li>
 *   <li>{@code excludeDates} – specific calendar dates to skip (holidays, cancelled classes).
 *       Any candidate lesson whose date appears here is silently skipped but does NOT
 *       consume a slot from {@code totalCount}.</li>
 *   <li>{@code slots} – each slot carries its own {@code date} (exact date+time of the first
 *       occurrence for WEEKLY, or the only occurrence for ONCE) plus {@code type} and optional
 *       {@code groupId}.</li>
 * </ul>
 */
public record LessonScheduleEntry(
    @NotNull RecurrenceType recurrence,
    OffsetDateTime startDate,
    @Min(1) Integer intervalWeeks,
    @NotNull @Min(1) Integer totalCount,
    List<LocalDate> excludeDates,
    @NotEmpty List<@Valid LessonSlot> slots
) {
    /** Returns excludeDates, never null. */
    public List<LocalDate> resolvedExcludeDates() {
        return excludeDates == null ? List.of() : excludeDates;
    }

    @AssertTrue(message = "intervalWeeks is required for WEEKLY recurrence")
    public boolean hasIntervalWeeksIfWeekly() {
        return recurrence != RecurrenceType.WEEKLY || intervalWeeks != null;
    }
}
