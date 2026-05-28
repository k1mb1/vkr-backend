package com.github.k1mb1.vkr_backend.attendance.checkin.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Фильтр для списка check-in сессий")
public record CheckInSessionFilter(
    @Schema(
        description = "ID разрешения преподавателя (обязательно). " + "Без других фильтров вернёт все сессии по предмету разрешения.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID permissionId,

    @Schema(description = "ID занятия. Если задан — только сессии scope'ов этого занятия")
    UUID lessonId,

    @Schema(description = "ID конкретного scope. Если задан — только сессии этого scope")
    UUID lessonScopeId
) {}
