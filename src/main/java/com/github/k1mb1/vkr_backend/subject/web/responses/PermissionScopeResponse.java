package com.github.k1mb1.vkr_backend.subject.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    description = "Scope права преподавателя — группа (с полным списком подгрупп) + опц. разрешённая подгруппа + опц. тип занятия"
)
public record PermissionScopeResponse(
    @Schema(description = "ID scope (null для синтетических scope при allPermissions=true)")
    UUID id,

    @Schema(description = "Группа с полным списком её подгрупп")
    PermissionScopeGroupResponse group,

    @Schema(description = "Разрешённая подгруппа (null = все подгруппы группы)")
    PermissionScopeSubgroupResponse allowedSubgroup,

    @Schema(description = "Разрешённый тип занятия (null = все типы)")
    LessonType allowedLessonType
) {}
