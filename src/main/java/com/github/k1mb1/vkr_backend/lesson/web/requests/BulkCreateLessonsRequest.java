package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

@Schema(
        description = "Запрос на массовое создание шаблонов занятий. "
                + "Создаются пустые шаблоны без scope'ов; scope'ы добавляются через POST /api/lessons/{id}/scopes.")
public record BulkCreateLessonsRequest(
        @Schema(description = "ID предмета", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID subjectId,

        @Schema(description = "Количество лекций", requiredMode = Schema.RequiredMode.REQUIRED) @PositiveOrZero int lectureCount,

        @Schema(description = "Количество практик", requiredMode = Schema.RequiredMode.REQUIRED) @PositiveOrZero int practiceCount) {
    @Schema(hidden = true)
    @AssertTrue(message = "At least one lesson must be requested") public boolean hasAnyLessonCount() {
        return lectureCount > 0 || practiceCount > 0;
    }
}
