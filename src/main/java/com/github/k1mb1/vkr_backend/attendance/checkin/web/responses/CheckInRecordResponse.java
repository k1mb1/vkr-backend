package com.github.k1mb1.vkr_backend.attendance.checkin.web.responses;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Запись о check-in одного студента")
public record CheckInRecordResponse(
    @Schema(description = "ID записи")
    UUID id,

    @Schema(description = "ID сессии")
    UUID sessionId,

    @Schema(description = "ID студента")
    UUID studentId,

    @Schema(description = "Статус")
    CheckInRecordStatus status,

    @Schema(description = "Момент отметки")
    Instant checkedInAt
) {}
