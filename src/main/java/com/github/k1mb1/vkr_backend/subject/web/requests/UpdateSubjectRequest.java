package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(name = "UpdateSubject", description = "Запрос на частичное обновление предмета")
public record UpdateSubjectRequest(
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
    String description,
    @Schema(description = "Флаг архивации предмета", example = "false")
    Boolean archived
) {}
