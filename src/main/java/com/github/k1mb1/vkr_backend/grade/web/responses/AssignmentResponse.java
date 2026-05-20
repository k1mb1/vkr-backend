package com.github.k1mb1.vkr_backend.grade.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Задание")
public record AssignmentResponse(
    @Schema(description = "ID задания") UUID id,
    @Schema(description = "ID занятия") UUID lessonId,
    @Schema(description = "Название") String title,
    @Schema(description = "Обязательное") boolean required,
    @Schema(description = "Максимальный балл") int maxScore
) {}
