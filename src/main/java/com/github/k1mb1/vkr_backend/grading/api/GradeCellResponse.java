package com.github.k1mb1.vkr_backend.grading.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Ячейка таблицы оценок (одна оценка)")
public record GradeCellResponse(
        @Schema(description = "ID записи") UUID id,

        @Schema(description = "ID студента") UUID studentId,

        @Schema(description = "ID занятия") UUID lessonId,

        @Schema(description = "ID задания (null = оценка вне задания)")
        UUID assignmentId,

        @Schema(
                description = "Активное занятие (того же типа, что и занятие задания) на момент выставления — "
                        + "для отображения. null — оценка вне задания или активного занятия не было.")
        UUID awardedLessonId,

        @Schema(
                description = "Знаковое смещение сдачи в занятиях своего типа (>0 — позже срока/штраф, "
                        + "<0 — раньше/бонус, 0 — вовремя), зафиксировано при выставлении. "
                        + "null — оценка вне задания или активного занятия не было. "
                        + "Фронт по нему применяет понижение/бонус.")
        Integer lessonsOffset,

        @Schema(description = "Балл (сырой, без понижения — понижение применяет фронт)")
        int score,

        @Schema(description = "Комментарий") String comment) {}
