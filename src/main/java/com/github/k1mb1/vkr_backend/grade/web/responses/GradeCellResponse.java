package com.github.k1mb1.vkr_backend.grade.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Ячейка таблицы оценок (одна оценка)")
public record GradeCellResponse(
    @Schema(description = "ID записи") UUID id,
    @Schema(description = "ID студента") UUID studentId,
    @Schema(description = "ID задания") UUID assignmentId,
    @Schema(description = "Оценка (> 0)") int value,
    @Schema(description = "Комментарий") String comment
) {}
