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

@Schema(
        description = "Массовое создание заданий для урока. Допустимо только если у урока ещё нет заданий; "
                + "иначе используйте PUT /api/assignments/{id} для обновления конкретного задания.")
public record CreateAssignmentsRequest(
        @NotNull @Schema(description = "ID занятия", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID lessonId,

        @NotEmpty @Valid @Size(max = 200) @Schema(
                description = "Список заданий. Порядковый номер (order) присваивается автоматически 1..N "
                        + "в соответствии с позицией в массиве.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<Item> items) {
    @Schema(name = "CreateAssignmentItem", description = "Описание одного задания в bulk-запросе")
    public record Item(
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
                name = "CreateAssignmentAdmissionTier",
                description = "Уровень допуска задания: ссылка на банду и минимальный балл")
        public record AdmissionTier(
                @NotNull @Schema(description = "ID банды итоговой аттестации", requiredMode = Schema.RequiredMode.REQUIRED)
                UUID bandId,

                @Min(0) @Max(1_000_000) @Schema(description = "Минимальный балл за задание для этой банды (включительно)")
                Integer minScore) {}
    }
}
