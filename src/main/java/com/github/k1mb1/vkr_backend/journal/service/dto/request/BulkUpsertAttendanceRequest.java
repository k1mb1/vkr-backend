package com.github.k1mb1.vkr_backend.journal.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = """
        Массовое создание/обновление ячеек посещаемости.
        Каждый элемент — целевое состояние одной ячейки (studentId, lessonScopeId, status, comment?).
        Пара (studentId, lessonScopeId) должна быть уникальна в пределах запроса.
        """)
public record BulkUpsertAttendanceRequest(
        @Schema(description = "Список ячеек", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid @Size(max = 5_000) List<UpsertAttendanceRequest> items) {}
