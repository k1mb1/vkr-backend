package com.github.k1mb1.vkr_backend.attendance.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для получения таблицы посещаемости")
public record AttendanceFilter(
        @Schema(
                description = "ID разрешения преподавателя (обязательно — определяет, что видно). "
                        + "Без других фильтров вернёт полную таблицу по предмету разрешения.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID permissionId,

        @Schema(
                description =
                        "ID занятия. Если задан — таблица только по scope'ам этого занятия (пересечённым с разрешением)")
        UUID lessonId,

        @Schema(description = "ID конкретного scope. Если задан — таблица только по одному столбцу")
        UUID lessonScopeId) {}
