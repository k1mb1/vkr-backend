package com.github.k1mb1.vkr_backend.grading.web.requests;

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
    @Schema(description = "ID занятия", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID lessonId,

    @Schema(
        description = "ID задания. null = оценка вне задания (одна на пару студент+занятие)",
        types = {"string", "null"}
    )
    UUID assignmentId,

    @Positive
    @Schema(
        description = "Балл (>0). Если задано assignmentId, должен быть ≤ maxPoints задания",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int score,

    @Schema(description = "Комментарий")
    String comment
) {}
