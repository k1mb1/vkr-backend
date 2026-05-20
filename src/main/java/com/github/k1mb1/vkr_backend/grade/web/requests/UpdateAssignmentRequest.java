package com.github.k1mb1.vkr_backend.grade.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Обновление задания")
public record UpdateAssignmentRequest(
    @NotBlank
    @Schema(description = "Название задания", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Positive
    @Schema(description = "Максимальный балл (> 0)", requiredMode = Schema.RequiredMode.REQUIRED)
    int maxScore,

    @Schema(description = "Обязательное задание")
    boolean required
) {}
