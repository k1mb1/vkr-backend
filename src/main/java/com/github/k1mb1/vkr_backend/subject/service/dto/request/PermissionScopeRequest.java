package com.github.k1mb1.vkr_backend.subject.service.dto.request;

import com.github.k1mb1.vkr_backend.subject.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(
        description = "Один scope права: группа (или все группы) + опциональный тип занятия. "
                + "Подгруппа допустима только внутри конкретной группы.")
public record PermissionScopeRequest(
        @Schema(
                description = "Группа scope'а; null = все группы предмета (тогда подгруппу указать нельзя)",
                types = {"object", "null"})
        @Valid PermissionScopeGroupRef group,

        @Schema(
                description = "Разрешённый тип занятия (null = все типы)",
                types = {"string", "null"})
        LessonType allowedLessonType) {
    @Schema(name = "PermissionScopeGroupRef", description = "Группа + опциональная подгруппа")
    public record PermissionScopeGroupRef(
            @Schema(description = "ID группы", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID groupId,

            @Schema(
                    description = "ID разрешённой подгруппы (null = вся группа)",
                    types = {"string", "null"})
            UUID allowedSubgroupId) {}
}
