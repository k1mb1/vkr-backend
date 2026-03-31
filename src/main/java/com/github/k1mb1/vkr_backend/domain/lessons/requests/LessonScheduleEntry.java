package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public record LessonScheduleEntry(
    @NotNull RecurrenceType recurrence,
    @NotNull OffsetDateTime startDate,
    @Min(1) Integer intervalWeeks,
    @NotNull @Min(1) Integer totalCount,
    @NotEmpty List<@Valid LessonSlot> slots
) {
    @AssertTrue(message = "intervalWeeks is required for WEEKLY recurrence")
    public boolean hasIntervalWeeksIfWeekly() {
        return recurrence != RecurrenceType.WEEKLY || intervalWeeks != null;
    }

    @AssertTrue(
        message = "weekIndex must be in range [0, intervalWeeks-1] for all WEEKLY slots"
    )
    public boolean hasValidWeekIndices() {
        if (
            recurrence != RecurrenceType.WEEKLY ||
            intervalWeeks == null ||
            slots == null
        ) return true;
        return slots
            .stream()
            .allMatch(s -> s.resolvedWeekIndex() < intervalWeeks);
    }
}
