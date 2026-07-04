package com.github.k1mb1.vkr_backend.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

@Schema(description = "Ошибка валидации конкретного поля запроса")
public record FieldError(
        @Schema(description = "Путь к полю", example = "students[0].username")
        String field,

        @Schema(description = "Имя нарушенного ограничения", example = "NotBlank") @Nullable String code,

        @Schema(description = "Человекочитаемое сообщение") @Nullable String message) {}
