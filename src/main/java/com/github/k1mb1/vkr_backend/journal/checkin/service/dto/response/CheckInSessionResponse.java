package com.github.k1mb1.vkr_backend.journal.checkin.service.dto.response;

import com.github.k1mb1.vkr_backend.journal.checkin.CheckInSessionState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Состояние check-in сессии для преподавателя")
public record CheckInSessionResponse(
        @Schema(description = "ID сессии") UUID id,

        @Schema(description = "ID шаблона занятия") UUID lessonId,

        @Schema(description = "ID проведения занятия (lesson_scope)")
        UUID lessonScopeId,

        @Schema(description = "Признак, что проведение охватывает все группы предмета.")
        boolean allGroups,

        @Schema(
                description = "Аудитория опроса — копия аудитории lesson scope. "
                        + "Если allGroups=true, содержит по одной записи на каждую группу предмета.")
        List<CheckInAudienceScopeResponse> audience,

        @Schema(
                description = "Код аудитории — преподаватель показывает его студентам; нужен им для отметки. "
                        + "В публичный ответ не отдаётся.")
        String code,

        @Schema(description = "Момент запуска") Instant startedAt,

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

        @Schema(description = "Текущее состояние сессии") CheckInSessionState state) {}
