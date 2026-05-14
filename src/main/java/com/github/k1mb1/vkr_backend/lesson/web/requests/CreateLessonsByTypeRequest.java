package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@Schema(description = "Запрос на создание занятий по количеству типов")
public record CreateLessonsByTypeRequest(
    @Schema(
        description = "ID предмета", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID subjectId,

    @Schema(
        description = "ID группы", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID groupId,

    @Schema(
        description = "Количество лекций", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @PositiveOrZero
    int lectureCount,

    @Schema(
        description = "Количество практик", requiredMode = Schema.RequiredMode.REQUIRED
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
