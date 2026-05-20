package com.github.k1mb1.vkr_backend.grade.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Создание или обновление одной ячейки оценки")
public record UpsertGradeRequest(
    @NotNull
    @Schema(description = "ID студента", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID studentId,

    @NotNull
    @Schema(description = "ID задания", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID assignmentId,

    @Positive
    @Schema(description = "Оценка (> 0)", requiredMode = Schema.RequiredMode.REQUIRED)
    int value,

    @Schema(description = "Комментарий")
    String comment
) {}
