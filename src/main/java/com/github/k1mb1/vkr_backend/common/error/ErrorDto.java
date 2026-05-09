package com.github.k1mb1.vkr_backend.common.error;

import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

@Builder
public record ErrorDto(
    int status,
    String message,
    Instant timestamp,
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
