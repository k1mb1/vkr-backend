package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на создание занятий по количеству типов")
public record CreateLessonsByTypeRequest(
    @Schema(
        description = "ID предмета",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID subjectId,

    @Schema(
        description = "true = занятия охватывают все группы предмета; false = только перечисленные в scopes",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    Boolean allGroups,

    @Schema(
        description = "Список scopes (group + опц. подгруппа). " +
            "Обязателен и должен быть непустым при allGroups=false. " +
            "При allGroups=true игнорируется."
    )
    @Valid
    List<LessonScopeRequest> scopes,

    @Schema(
        description = "Количество лекций",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @PositiveOrZero
    int lectureCount,

    @Schema(
        description = "Количество практик",
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

    @Schema(hidden = true)
    @AssertTrue(message = "scopes must be non-empty when allGroups=false")
    public boolean hasScopesWhenNotAllGroups() {
        return Boolean.TRUE.equals(allGroups) || (scopes != null && !scopes.isEmpty());
    }
}
