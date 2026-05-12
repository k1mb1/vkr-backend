package com.github.k1mb1.vkr_backend.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Builder
@Schema(description = "Стандартная модель ошибки API")
public record ErrorDto(
    @Schema(description = "HTTP статус код")
    int status,

    @Schema(description = "Сообщение об ошибке")
    String message,

    @Schema(description = "Время возникновения ошибки")
    Instant timestamp,

    @Schema(description = "Дополнительные детали ошибки (например, список валидационных ошибок)")
    String details
) {
    public static ErrorDto of(@NonNull String message, @NonNull HttpStatus status) {
        return ErrorDto.builder()
            .status(status.value())
            .message(message)
            .timestamp(Instant.now())
            .build();
    }

    public static ErrorDto of(@NonNull String message, @NonNull HttpStatus status, String details) {
        return ErrorDto.builder()
            .status(status.value())
            .message(message)
            .timestamp(Instant.now())
            .details(details)
            .build();
    }
}
