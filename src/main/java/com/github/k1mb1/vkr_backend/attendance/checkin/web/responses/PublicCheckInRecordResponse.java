package com.github.k1mb1.vkr_backend.attendance.checkin.web.responses;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(
    description = "Подтверждение собственной отметки студента. " +
        "Содержит только его собственный результат — ни ID записи, ни данные других студентов не раскрываются."
)
public record PublicCheckInRecordResponse(
    @Schema(description = "Статус собственной отметки (вовремя / с опозданием)")
    CheckInRecordStatus status,

    @Schema(description = "Момент отметки")
    Instant checkedInAt
) {}
