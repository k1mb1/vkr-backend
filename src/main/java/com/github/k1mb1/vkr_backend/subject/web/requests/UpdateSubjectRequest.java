package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на частичное обновление предмета")
public record UpdateSubjectRequest(
    @Schema(
        description = "Название предмета", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String name,

    @Schema(description = "Описание предмета")
    String description,

    @Schema(description = "Флаг архивации предмета")
    Boolean archived
) {}
