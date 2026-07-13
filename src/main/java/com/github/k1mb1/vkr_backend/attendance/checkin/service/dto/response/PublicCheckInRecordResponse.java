package com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;

@Builder
@Schema(
        description =
                "Подтверждение собственной отметки студента. "
                        + "Содержит только его собственный результат — ни ID записи, ни данные других студентов не раскрываются.")
public record PublicCheckInRecordResponse(
        @Schema(description = "Статус собственной отметки (вовремя / с опозданием)")
        CheckInRecordStatus status,

        @Schema(description = "Момент отметки") Instant checkedInAt) {}
