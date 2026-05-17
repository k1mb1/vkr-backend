package com.github.k1mb1.vkr_backend.attendance.web.responses;

import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Ячейка таблицы посещаемости (одна отметка)")
public record AttendanceCellResponse(
    @Schema(description = "ID записи") UUID id,

    @Schema(description = "ID студента") UUID studentId,

    @Schema(description = "ID занятия") UUID lessonId,

    @Schema(description = "Статус") AttendanceStatus status,

    @Schema(description = "Комментарий") String comment
) {}
