package com.github.k1mb1.vkr_backend.lesson.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Аудитория scope занятия: группа + опциональная подгруппа")
public record LessonScopeAudienceRequest(
        @Schema(description = "ID группы", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID groupId,

        @Schema(
                description = "ID разрешённой подгруппы (null = вся группа)",
                types = {"string", "null"})
        UUID allowedSubgroupId) {}
