package com.github.k1mb1.vkr_backend.attendance.checkin.web.responses;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(
    description = "Публичное состояние check-in сессии (для страницы студентов). " +
        "Ростер группы намеренно не отдаётся: чтобы отметиться, студент ищет себя по фамилии " +
        "через GET /{id}/students?query=... — так список группы и статусы посещаемости " +
        "не раскрываются всем по ссылке."
)
public record PublicCheckInSessionResponse(
    @Schema(description = "ID сессии") UUID id,

    @Schema(description = "Тема занятия") String lessonTopic,

    @Schema(
        description = "Аудитория опроса — копия lesson scopes (группа + опц. подгруппа). " +
            "Если allGroups=true, содержит по одной записи на каждую группу предмета."
    )
    List<CheckInAudienceScope> audience,

    @Schema(description = "Текущее состояние сессии") CheckInSessionState state,

    @Schema(description = "Момент окончания основного окна")
    Instant onTimeEndsAt,

    @Schema(description = "Момент окончания окна для опоздавших")
    Instant lateEndsAt,

    @Schema(description = "Текущий момент сервера (UTC)") Instant serverNow
) {}
