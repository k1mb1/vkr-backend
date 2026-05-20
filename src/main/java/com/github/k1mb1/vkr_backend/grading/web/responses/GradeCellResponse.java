package com.github.k1mb1.vkr_backend.grading.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Ячейка таблицы оценок (одна оценка)")
public record GradeCellResponse(
    @Schema(description = "ID записи")
    UUID id,

    @Schema(description = "ID студента")
    UUID studentId,

    @Schema(description = "ID занятия")
    UUID lessonId,

    @Schema(description = "ID задания (null = оценка вне задания)")
    UUID assignmentId,

    @Schema(description = "Балл")
    int score,

    @Schema(description = "Комментарий")
    String comment
) {}
