package com.github.k1mb1.vkr_backend.lesson.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Проведение занятия — группа/подгруппа (или все группы) + дата")
public record LessonScopeResponse(
    @Schema(description = "ID scope")
    UUID id,

    @Schema(description = "ID группы (null = все группы предмета)")
    UUID groupId,

    @Schema(description = "Название группы (null = все группы предмета)")
    String groupName,

    @Schema(description = "ID разрешённой подгруппы")
    UUID allowedSubgroupId,

    @Schema(description = "Индекс разрешённой подгруппы")
    Integer allowedSubgroupIndex,

    @Schema(description = "Дата проведения занятия")
    LocalDate startedAt,

    @Schema(description = "true = проведение для всех групп предмета сразу")
    boolean allGroups
) {}
