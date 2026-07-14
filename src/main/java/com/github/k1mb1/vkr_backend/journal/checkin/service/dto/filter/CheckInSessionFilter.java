package com.github.k1mb1.vkr_backend.journal.checkin.service.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для списка check-in сессий")
public record CheckInSessionFilter(
        @Schema(
                description = "ID разрешения преподавателя (обязательно). "
                        + "Без других фильтров вернёт все сессии по предмету разрешения.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID permissionId,

        @Schema(description = "ID занятия. Если задан — только сессии scope'ов этого занятия")
        UUID lessonId,

        @Schema(description = "ID конкретного scope. Если задан — только сессии этого scope")
        UUID lessonScopeId) {}
