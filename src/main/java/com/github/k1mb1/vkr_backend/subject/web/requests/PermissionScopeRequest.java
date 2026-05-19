package com.github.k1mb1.vkr_backend.subject.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(
    description = "Один scope права: группа + опциональная подгруппа + опциональный тип занятия"
)
public record PermissionScopeRequest(
    @Schema(
        description = "ID группы",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID groupId,

    @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
    UUID allowedSubgroupId,

    @Schema(description = "Разрешённый тип занятия (null = все типы)")
    LessonType allowedLessonType
) {}
