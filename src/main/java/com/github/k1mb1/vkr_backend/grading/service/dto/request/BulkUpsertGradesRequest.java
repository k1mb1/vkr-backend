package com.github.k1mb1.vkr_backend.grading.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = """
        Массовое создание/обновление ячеек оценок.
        Каждый элемент — целевое состояние одной ячейки (studentId, lessonId, assignmentId?, score, comment?).
        Тройка (studentId, lessonId, assignmentId) должна быть уникальна в пределах запроса
        (assignmentId=null — отдельная категория «оценка вне задания»).
        """)
public record BulkUpsertGradesRequest(
        @Schema(description = "Список ячеек", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty @Valid List<UpsertGradeRequest> items) {}
