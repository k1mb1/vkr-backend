package com.github.k1mb1.vkr_backend.apis.error;

import java.time.Instant;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;

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
        return new ErrorDto(
            status.value(),
            message,
            Instant.now(),
            null
        );
    }

    public static ErrorDto of(
        @NonNull String message,
        @NonNull HttpStatus status,
        String details
    ) {
        return new ErrorDto(
            status.value(),
            message,
            Instant.now(),
            details
        );
    }
}
