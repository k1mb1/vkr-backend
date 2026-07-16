package com.github.k1mb1.vkr_backend.journal.service.dto.request;

import com.github.k1mb1.vkr_backend.journal.AssignmentAdmissionMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = """
    Массовое обновление заданий по id. В одном запросе можно править задания
    нескольких уроков. Внутри одного урока итоговые порядковые номера должны быть уникны.
    """)
public record BulkUpdateAssignmentsRequest(
        @Schema(description = "Список целевых состояний", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid @Size(max = 200) List<Item> items) {
    @Schema(name = "UpdateAssignmentItem", description = "Целевое состояние задания")
    public record Item(
            @Schema(description = "ID задания", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID id,

            @Positive @Max(1_000) @Schema(
                    description = "Порядковый номер в рамках занятия (1..N)",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            int order,

            @Positive @Max(1_000_000) @Schema(description = "Максимальное количество баллов (>0)", requiredMode = Schema.RequiredMode.REQUIRED)
            int maxPoints,

            @Schema(description = "Обязательное ли задание", requiredMode = Schema.RequiredMode.REQUIRED)
            boolean required,

            @Schema(description = "Режим допуска: используется ли задание как условие для итоговой оценки")
            AssignmentAdmissionMode admissionMode,

            @Min(0) @Max(1_000_000) @Schema(description = "MIN_SCORE: минимальный балл для прохождения допуска")
            Integer admissionMinScore,

            @Valid @Size(max = 100) @Schema(description = "TIERED: уровни допуска по убыванию старшинства (ссылки на банды)")
            List<AdmissionTier> admissionTiers) {
        @Schema(
                name = "UpdateAssignmentAdmissionTier",
                description = "Уровень допуска задания: ссылка на банду и минимальный балл")
        public record AdmissionTier(
                @NotNull @Schema(description = "ID банды итоговой аттестации", requiredMode = Schema.RequiredMode.REQUIRED)
                UUID bandId,

                @Min(0) @Max(1_000_000) @Schema(description = "Минимальный балл за задание для этой банды (включительно)")
                Integer minScore) {}
    }
}
