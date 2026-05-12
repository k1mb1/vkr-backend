package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

@Schema(name = "CreateLessonsByType", description = "Запрос на создание занятий по количеству типов")
public record CreateLessonsByTypeRequest(
    @Schema(
        description = "ID предмета",
        example = "550e8400-e29b-41d4-a716-446655440002",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID subjectId,
    @Schema(
        description = "Количество лекций",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @PositiveOrZero
    int lectureCount,
    @Schema(
        description = "Количество практик",
        example = "8",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @PositiveOrZero
    int practiceCount
) {
    @Schema(hidden = true)
    @AssertTrue(message = "At least one lesson must be requested")
    public boolean hasAnyLessonCount() {
        return lectureCount > 0 || practiceCount > 0;
    }
}
