package com.github.k1mb1.vkr_backend.grade.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(description = "Создание задания")
public record CreateAssignmentRequest(
    @NotNull
    @Schema(description = "ID занятия", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID lessonId,

    @NotBlank
    @Schema(description = "Название задания", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Positive
    @Schema(description = "Максимальный балл (> 0)", requiredMode = Schema.RequiredMode.REQUIRED)
    int maxScore,

    @Schema(description = "Обязательное задание", defaultValue = "false")
    boolean required
) {}
