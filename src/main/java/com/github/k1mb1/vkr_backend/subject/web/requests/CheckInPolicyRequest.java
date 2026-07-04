package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
@Schema(description = """
        Единое время на отметку (check-in) для всего предмета.
        Если enabled = true — onTimeSeconds и lateSeconds обязательны (проверяется на сервисе),
        и любая check-in сессия предмета использует именно эти окна, игнорируя значения из запроса
        на запуск. Если enabled = false — окна задаются индивидуально при запуске сессии.
        """)
public record CheckInPolicyRequest(
        @Schema(description = "Включена ли единая политика окон check-in", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean enabled,

        @Schema(
                description = "Длительность основного окна (секунды). Отметившиеся здесь получат статус PRESENT",
                example = "600")
        @Min(1) Integer onTimeSeconds,

        @Schema(
                description = "Дополнительное окно для опоздавших (секунды). Отметившиеся здесь получат статус LATE",
                example = "600")
        @PositiveOrZero Integer lateSeconds) {}
