package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на создание шаблонов занятий по количеству типов с начальной аудиторией")
public record CreateLessonsByTypeRequest(
    @Schema(
        description = "ID предмета", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID subjectId,

    @Schema(
        description = "Список scopes (группа+опц.подгруппа или allGroups) с датой проведения. " + "К каждому созданному шаблону прикрепляются все указанные scopes.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    @Valid
    List<LessonScopeRequest> scopes,

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
