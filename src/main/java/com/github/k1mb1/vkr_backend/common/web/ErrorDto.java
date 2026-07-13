package com.github.k1mb1.vkr_backend.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

@Builder
@Schema(description = "Стандартная модель ошибки API")
public record ErrorDto(
        @Schema(description = "HTTP статус код") int status,

        @Schema(description = "Машинно-читаемый код ошибки") ErrorCode code,

        @Schema(description = "Человекочитаемое сообщение об ошибке") @Nullable String message,

        @Schema(description = "Время возникновения ошибки") Instant timestamp,

        @Schema(description = "Список ошибок по полям запроса (только для VALIDATION_FAILED / INVALID_PARAM)")
        List<FieldError> fieldErrors) {
    public static ErrorDto of(@NonNull ErrorCode code, @Nullable String message, @NonNull HttpStatus status) {
        return ErrorDto.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorDto of(
            @NonNull ErrorCode code,
            @Nullable String message,
            @NonNull HttpStatus status,
            List<FieldError> fieldErrors) {
        return ErrorDto.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .fieldErrors(fieldErrors)
                .build();
    }
}
