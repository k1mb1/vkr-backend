package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateLessonsByTypeRequest(
        @NotNull UUID subjectId,
        @PositiveOrZero int lectureCount,
    @PositiveOrZero int practiceCount
) {

    @AssertTrue(message = "At least one lesson must be requested")
    public boolean hasAnyLessonCount() {
        return lectureCount > 0 || practiceCount > 0;
    }
}
