package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(
    description = """
    Запрос на частичное обновление предмета (PATCH).
    Семантика: поле = null или отсутствует — значение не изменяется.
    Исключение: name обязателен (валидируется как @NotBlank).
    """
)
public record UpdateSubjectRequest(
    @Schema(
        description = "Название предмета",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String name,

    @Schema(description = "Описание предмета (null — не менять)")
    String description,

    @Schema(
        description = "Флаг архивации: true — архивировать, false — разархивировать, null — не менять"
    )
    Boolean archived
) {}
