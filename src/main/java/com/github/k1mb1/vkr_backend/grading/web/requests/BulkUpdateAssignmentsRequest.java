package com.github.k1mb1.vkr_backend.grading.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

@Schema(
    description = """
        Массовое обновление заданий по id. В одном запросе можно править задания
        нескольких уроков. Внутри одного урока итоговые порядковые номера должны быть уникальны.
        """
)
public record BulkUpdateAssignmentsRequest(
    @Schema(description = "Список целевых состояний", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    List<Item> items
) {
    @Schema(description = "Целевое состояние задания")
    public record Item(
        @Schema(description = "ID задания", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID id,

        @Positive
        @Schema(
            description = "Порядковый номер в рамках занятия (1..N)",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        int order,

        @Positive
        @Schema(
            description = "Максимальное количество баллов (>0)",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        int maxPoints,

        @Schema(description = "Обязательное ли задание", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean required
    ) {}
}
