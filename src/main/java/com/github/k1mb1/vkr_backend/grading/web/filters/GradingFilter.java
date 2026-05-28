package com.github.k1mb1.vkr_backend.grading.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Фильтр для получения таблицы оценок")
public record GradingFilter(
    @Schema(
        description = "ID разрешения преподавателя (обязательно — определяет, что видно). " + "Без других фильтров вернёт полную таблицу по предмету разрешения.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID permissionId,

    @Schema(description = "ID занятия. Если задан — таблица только по этому занятию")
    UUID lessonId,

    @Schema(description = "ID конкретного scope. Если задан — таблица только по уроку этого scope")
    UUID lessonScopeId
) {}
