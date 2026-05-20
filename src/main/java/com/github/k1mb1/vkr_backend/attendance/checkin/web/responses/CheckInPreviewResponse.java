package com.github.k1mb1.vkr_backend.attendance.checkin.web.responses;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(
    description = "Сверочный пред-просмотр результатов check-in. Преподаватель видит, " + "что попадёт в основную посещаемость после подтверждения."
)
public record CheckInPreviewResponse(
    @Schema(description = "Состояние сессии")
    CheckInSessionResponse session,

    @Schema(description = "Строки сверки: одна на каждого студента")
    List<Row> rows
) {
    @Schema(description = "Строка сверки")
    public record Row(
        @Schema(description = "ID студента")
        UUID studentId,

        @Schema(description = "Имя студента")
        String username,

        @Schema(description = "Статус из check-in (если был)")
        CheckInRecordStatus checkInStatus,

        @Schema(description = "Момент отметки (если был)")
        Instant checkedInAt,

        @Schema(
            description = "Какой статус посещаемости будет проставлен по умолчанию"
        )
        AttendanceStatus proposedStatus
    ) {}
}
