package com.github.k1mb1.vkr_backend.journal.service.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр страницы итогов (оценки + посещаемость одним запросом)")
public record ResultsFilter(
        @Schema(
                description = "ID разрешения преподавателя (обязательно — определяет, что видно)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID permissionId,

        @Schema(description = "ID занятия. Если задан — таблицы только по этому занятию")
        UUID lessonId) {}
