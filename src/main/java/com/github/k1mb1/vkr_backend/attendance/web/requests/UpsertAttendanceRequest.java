package com.github.k1mb1.vkr_backend.attendance.web.requests;

import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Создание или обновление одной ячейки посещаемости")
public record UpsertAttendanceRequest(
    @NotNull
    @Schema(
        description = "ID студента", requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID studentId,

    @NotNull
    @Schema(
        description = "ID проведения занятия (lesson_scope)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID lessonScopeId,

    @NotNull
    @Schema(description = "Статус", requiredMode = Schema.RequiredMode.REQUIRED)
    AttendanceStatus status,

    @Schema(description = "Комментарий")
    String comment
) {}
