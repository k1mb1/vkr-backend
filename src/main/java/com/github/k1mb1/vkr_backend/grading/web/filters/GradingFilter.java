package com.github.k1mb1.vkr_backend.grading.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Фильтр для получения таблицы оценок")
public record GradingFilter(
    @Schema(
        description = "ID разрешения преподавателя. Вернёт таблицу по предмету разрешения: " + "студенты — объединение по scopes разрешения, занятия — те, у которых allGroups=true " + "или есть scope, пересекающийся хотя бы с одним scope разрешения.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID permissionId
) {}
