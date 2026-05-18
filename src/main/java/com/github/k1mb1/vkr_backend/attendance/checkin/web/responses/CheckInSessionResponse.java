package com.github.k1mb1.vkr_backend.attendance.checkin.web.responses;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Состояние check-in сессии для преподавателя")
public record CheckInSessionResponse(
    @Schema(description = "ID сессии")
    UUID id,

    @Schema(description = "ID занятия")
    UUID lessonId,

    @Schema(description = "ID права преподавателя на предмет")
    UUID permissionId,

    @Schema(description = "Момент запуска")
    Instant startedAt,

    @Schema(description = "Длительность основного окна (с)")
    int onTimeSeconds,

    @Schema(description = "Длительность окна для опоздавших (с)")
    int lateSeconds,

    @Schema(description = "Момент окончания основного окна")
    Instant onTimeEndsAt,

    @Schema(description = "Момент окончания окна для опоздавших")
    Instant lateEndsAt,

    @Schema(description = "Момент подтверждения (если уже подтверждена)")
    Instant confirmedAt,

    @Schema(description = "Момент отмены (если отменена)")
    Instant cancelledAt,

    @Schema(description = "Текущее состояние сессии")
    CheckInSessionState state
) {}
