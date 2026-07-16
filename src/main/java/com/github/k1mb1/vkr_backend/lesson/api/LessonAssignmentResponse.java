package com.github.k1mb1.vkr_backend.lesson.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Задание занятия в составе {@code LessonResponse}. Собственная проекция модуля
 * lesson: заполняется модулем journal через {@link LessonAssignmentsPort}
 * (инверсия зависимости — lesson не знает о journal), форма JSON совпадает с
 * {@code AssignmentResponse} модуля journal.
 */
@Schema(name = "LessonAssignment", description = "Задание занятия")
public record LessonAssignmentResponse(
        @Schema(description = "ID задания") UUID id,
        @Schema(description = "ID занятия") UUID lessonId,
        @Schema(description = "Порядковый номер задания") int order,

        @Schema(description = "Максимальное количество баллов")
        int maxPoints,

        @Schema(description = "Обязательное ли задание") boolean required,

        @Schema(description = "Режим допуска: используется ли задание как условие для итоговой оценки")
        AdmissionMode admissionMode,

        @Schema(description = "MIN_SCORE: минимальный балл для прохождения допуска") @Nullable Integer admissionMinScore,

        @Schema(description = "TIERED: уровни допуска по убыванию старшинства (ссылки на банды)") @Nullable List<AdmissionTier> admissionTiers) {

    /** Зеркало {@code AssignmentAdmissionMode} модуля journal (JSON-совместимо по именам). */
    public enum AdmissionMode {
        NONE,
        PASS_FAIL,
        MIN_SCORE,
        TIERED,
    }

    @Schema(name = "LessonAssignmentAdmissionTier", description = "Уровень допуска задания")
    public record AdmissionTier(
            @Schema(description = "ID банды итоговой аттестации")
            UUID bandId,

            @Schema(description = "Минимальный балл за задание для этой банды (включительно)") @Nullable Integer minScore) {}
}
