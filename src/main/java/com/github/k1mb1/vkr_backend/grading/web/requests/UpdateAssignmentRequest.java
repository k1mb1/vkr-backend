package com.github.k1mb1.vkr_backend.grading.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Обновление задания (lessonId менять нельзя — задание привязано к уроку при создании)")
public record UpdateAssignmentRequest(
    @Positive
    @Schema(
        description = "Порядковый номер задания в рамках занятия (1..N)",
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
