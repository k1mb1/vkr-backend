package com.github.k1mb1.vkr_backend.grade.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
    description = "Аудитория таблицы оценок — одна запись на (группа, опц. подгруппа)"
)
public record GradeAudienceScope(
    @Schema(description = "ID группы") UUID groupId,
    @Schema(description = "Название группы") String groupName,
    @Schema(description = "ID разрешённой подгруппы (null = вся группа)") UUID allowedSubgroupId,
    @Schema(description = "Индекс разрешённой подгруппы (null = вся группа)") Integer allowedSubgroupIndex
) {}
