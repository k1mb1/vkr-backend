package com.github.k1mb1.vkr_backend.lesson.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Scope занятия — группа + опц. подгруппа")
public record LessonScopeResponse(
    @Schema(description = "ID scope") UUID id,

    @Schema(description = "ID группы") UUID groupId,

    @Schema(description = "Название группы") String groupName,

    @Schema(description = "ID разрешённой подгруппы") UUID allowedSubgroupId,

    @Schema(description = "Индекс разрешённой подгруппы")
    Integer allowedSubgroupIndex
) {}
