package com.github.k1mb1.vkr_backend.journal.service.dto.request;

import com.github.k1mb1.vkr_backend.journal.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
@Schema(description = "Создание или обновление одной ячейки посещаемости")
public record UpsertAttendanceRequest(
        @NotNull @Schema(description = "ID студента", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID studentId,

        @NotNull @Schema(description = "ID проведения занятия (lesson_scope)", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID lessonScopeId,

        @NotNull @Schema(description = "Статус", requiredMode = Schema.RequiredMode.REQUIRED)
        AttendanceStatus status,

        @Schema(description = "Комментарий") @Nullable String comment) {}
