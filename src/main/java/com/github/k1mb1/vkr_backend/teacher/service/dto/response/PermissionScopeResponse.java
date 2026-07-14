package com.github.k1mb1.vkr_backend.teacher.service.dto.response;

import com.github.k1mb1.vkr_backend.subject.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
@Schema(
        description =
                "Scope права преподавателя — группа (с полным списком подгрупп) + опц. разрешённая подгруппа + опц. тип занятия")
public record PermissionScopeResponse(
        @Schema(description = "ID scope (null для синтетических scope при allPermissions=true)") @Nullable UUID id,

        @Schema(description = "Группа с полным списком её подгрупп") @Nullable PermissionScopeGroupResponse group,

        @Schema(description = "Разрешённая подгруппа (null = все подгруппы группы)") @Nullable PermissionScopeSubgroupResponse allowedSubgroup,

        @Schema(description = "Разрешённый тип занятия (null = все типы)") @Nullable LessonType allowedLessonType) {}
