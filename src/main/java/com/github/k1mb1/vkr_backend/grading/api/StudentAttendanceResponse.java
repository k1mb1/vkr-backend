package com.github.k1mb1.vkr_backend.grading.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Сводка посещаемости студента по занятиям таблицы (для расчёта вклада на фронте)")
public record StudentAttendanceResponse(
        @Schema(description = "ID студента") UUID studentId,

        @Schema(description = "Кол-во присутствий (PRESENT)")
        int present,

        @Schema(description = "Кол-во опозданий (LATE)") int late,

        @Schema(description = "Кол-во пропусков без причины (ABSENT)")
        int absent,

        @Schema(description = "Кол-во пропусков по уважительной причине (EXCUSED)")
        int excused) {}
