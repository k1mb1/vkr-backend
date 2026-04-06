package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Bulk(
        @NotNull UUID subjectId,
        @NotEmpty List<@Valid Entry> schedules
) {
    public record Entry(
            @NotNull LessonType type,
            @NotNull LocalDate startDate,
            @NotNull @Min(1) Integer totalCount,
            @NotEmpty List<@NotNull List<@NotNull DayOfWeek>> daysOfWeek
    ) {
    }
}
