package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
    description = "Scope занятия: группа + опциональная подгруппа"
)
public record LessonScopeRequest(
    @Schema(
        description = "ID группы", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID groupId,

    @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
    UUID allowedSubgroupId
) {}
