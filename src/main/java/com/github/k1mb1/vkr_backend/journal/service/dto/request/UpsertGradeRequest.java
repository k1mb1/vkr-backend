package com.github.k1mb1.vkr_backend.journal.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Создание или обновление одной ячейки оценки")
public record UpsertGradeRequest(
        @NotNull @Schema(description = "ID студента", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID studentId,

        @NotNull @Schema(description = "ID занятия", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID lessonId,

        @Schema(
                description = "ID задания. null = оценка вне задания (одна на пару студент+занятие)",
                types = {"string", "null"})
        UUID assignmentId,

        // @Min(0), а не @Positive: нулевая оценка — легальное значение (раньше @Positive её запрещал).
        // Верхнюю границу даёт maxPoints задания в сервисе; здесь — лишь разумный абсолютный потолок.
        @Min(0) @Max(1_000_000) @Schema(
                description = "Балл (≥0). Если задано assignmentId, должен быть ≤ maxPoints задания",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int score,

        @Size(max = 1_000) @Schema(description = "Комментарий")
        String comment) {}
