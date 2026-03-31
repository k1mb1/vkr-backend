package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public record LessonSlot(
    @NotNull LessonType type,
    @Min(0) Integer weekIndex,
    @NotEmpty List<@NotNull DayOfWeek> daysOfWeek,
    UUID groupId
) {
    public int resolvedWeekIndex() {
        return weekIndex == null ? 0 : weekIndex;
    }
}
