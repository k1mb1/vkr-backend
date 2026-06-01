package com.github.k1mb1.vkr_backend.attendance.checkin.web.responses;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(
    description = "Публичное состояние check-in сессии (для страницы студентов)"
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

    @Schema(description = "Текущий момент сервера (UTC)") Instant serverNow,

    @Schema(description = "Список студентов для таблицы check-in")
    List<Student> students
) {
    @Schema(description = "Строка таблицы студентов")
    public record Student(
        @Schema(description = "ID студента") UUID id,

        @Schema(
            description = "Маскированное ФИО студента (фамилия + инициалы, напр. «Иванов И. И.») — " +
                "публичный эндпоинт не отдаёт полное имя в целях защиты персональных данных"
        )
        String username,

        @Schema(
            description = "Уже отметившийся статус, либо null если ещё не отмечен"
        )
        CheckInRecordStatus checkedInStatus,

        @Schema(description = "Момент отметки, если есть") Instant checkedInAt
    ) {}
}
