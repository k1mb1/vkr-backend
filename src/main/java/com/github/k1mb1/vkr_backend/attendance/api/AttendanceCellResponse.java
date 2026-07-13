package com.github.k1mb1.vkr_backend.attendance.api;

import com.github.k1mb1.vkr_backend.attendance.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Ячейка таблицы посещаемости (одна отметка)")
public record AttendanceCellResponse(
        @Schema(description = "ID записи") UUID id,

        @Schema(description = "ID студента") UUID studentId,

        @Schema(description = "ID проведения занятия (lesson_scope)")
        UUID lessonScopeId,

        @Schema(description = "Статус") AttendanceStatus status,

        @Schema(description = "Комментарий") String comment) {}
