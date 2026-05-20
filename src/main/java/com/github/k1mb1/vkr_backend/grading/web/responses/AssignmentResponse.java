package com.github.k1mb1.vkr_backend.grading.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Задание в таблице оценок")
public record AssignmentResponse(
    @Schema(description = "ID задания")
    UUID id,

    @Schema(description = "ID занятия")
    UUID lessonId,

    @Schema(description = "Порядковый номер задания")
    int order,

    @Schema(description = "Максимальное количество баллов")
    int maxPoints,

    @Schema(description = "Обязательное ли задание")
    boolean required
) {}
