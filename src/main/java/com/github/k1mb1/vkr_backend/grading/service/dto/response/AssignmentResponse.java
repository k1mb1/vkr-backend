package com.github.k1mb1.vkr_backend.grading.service.dto.response;

import com.github.k1mb1.vkr_backend.grading.domain.AssignmentAdmissionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Задание в таблице оценок")
public record AssignmentResponse(
        @Schema(description = "ID задания") UUID id,

        @Schema(description = "ID занятия") UUID lessonId,

        @Schema(description = "Порядковый номер задания") int order,

        @Schema(description = "Максимальное количество баллов")
        int maxPoints,

        @Schema(description = "Обязательное ли задание") boolean required,

        @Schema(description = "Режим допуска: используется ли задание как условие для итоговой оценки")
        AssignmentAdmissionMode admissionMode,

        @Schema(description = "MIN_SCORE: минимальный балл для прохождения допуска")
        Integer admissionMinScore,

        @Schema(description = "TIERED: уровни допуска по убыванию старшинства (ссылки на банды)")
        List<AdmissionTier> admissionTiers) {
    @Schema(
            name = "AssignmentAdmissionTier",
            description = "Уровень допуска задания: ссылка на банду и минимальный балл")
    public record AdmissionTier(
            @Schema(description = "ID банды итоговой аттестации")
            UUID bandId,

            @Schema(description = "Минимальный балл за задание для этой банды (включительно)")
            Integer minScore) {}
}
