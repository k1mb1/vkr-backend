package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateSubject", description = "Запрос на создание предмета")
public record CreateSubjectRequest(
    @Schema(
        description = "Название предмета",
        example = "Математический анализ",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String name,
    @Schema(
        description = "Описание предмета",
        example = "Базовый курс математического анализа"
    )
    String description
) {}
