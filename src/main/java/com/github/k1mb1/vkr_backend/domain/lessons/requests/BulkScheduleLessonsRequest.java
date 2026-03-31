package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record BulkScheduleLessonsRequest(
    @NotNull UUID subjectId,
    @NotEmpty List<@Valid LessonScheduleEntry> schedules
) {}
