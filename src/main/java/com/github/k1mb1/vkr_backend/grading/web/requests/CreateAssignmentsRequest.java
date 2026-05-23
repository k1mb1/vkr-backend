package com.github.k1mb1.vkr_backend.grading.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

@Schema(
    description = "Массовое создание заданий для урока. Допустимо только если у урока ещё нет заданий; " + "иначе используйте PUT /api/assignments/{id} для обновления конкретного задания."
)
public record CreateAssignmentsRequest(
    @NotNull
    @Schema(description = "ID занятия", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID lessonId,

    @NotEmpty
    @Valid
    @Schema(
        description = "Список заданий. Порядковый номер (order) присваивается автоматически 1..N " + "в соответствии с позицией в массиве.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    List<Item> items
) {
    @Schema(description = "Описание одного задания в bulk-запросе")
    public record Item(
        @Positive
        @Schema(
            description = "Максимальное количество баллов (>0)",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        int maxPoints,

        @Schema(
            description = "Обязательное ли задание", requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean required
    ) {}
}
