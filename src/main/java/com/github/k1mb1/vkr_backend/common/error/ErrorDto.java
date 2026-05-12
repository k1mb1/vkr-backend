package com.github.k1mb1.vkr_backend.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

@Builder
@Schema(name = "Error", description = "Стандартная модель ошибки API")
public record ErrorDto(
    @Schema(description = "HTTP статус код", example = "404") int status,
    @Schema(description = "Сообщение об ошибке", example = "Entity not found")
    String message,
    @Schema(
        description = "Время возникновения ошибки",
        example = "2024-01-01T12:00:00Z"
    )
    Instant timestamp,
    @Schema(
        description = "Дополнительные детали ошибки (например, список валидационных ошибок)",
        example = "name: must not be blank"
    )
    String details
) {
    public static ErrorDto of(
        @NonNull String message,
        @NonNull HttpStatus status
    ) {
        return ErrorDto.builder()
            .status(status.value())
            .message(message)
            .timestamp(Instant.now())
            .build();
    }

    public static ErrorDto of(
        @NonNull String message,
        @NonNull HttpStatus status,
        String details
    ) {
        return ErrorDto.builder()
            .status(status.value())
            .message(message)
            .timestamp(Instant.now())
            .details(details)
            .build();
    }
}
