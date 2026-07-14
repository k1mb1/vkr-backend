package com.github.k1mb1.vkr_backend.subject.service.dto.request;

import com.github.k1mb1.vkr_backend.subject.AttendanceMode;
import com.github.k1mb1.vkr_backend.subject.AttendanceRequirementMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = """
    Параметры промежуточной аттестации (итоги): единый список банд + режим посещаемости.
    При enabled = true обязателен непустой bands (проверяется валидатором ниже, не только
    на сервисе). Каждая банда — ярлык и условия (баллы и/или задачи), условия комбинируются
    по AND. Банды нужно слать по убыванию старшинства. Все вычисления вердикта и «сколько ещё»
    делает фронт.
    """)
public record FinalAssessmentPolicyRequest(
        @Schema(description = "Включены ли итоги (банды)", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull Boolean enabled,

        @Size(max = 50) @Schema(description = "Банды по убыванию старшинства (первая — самая старшая)") @Valid List<Band> bands,

        @Schema(
                description =
                        "Учёт посещаемости: COMBINED (в балл) или SEPARATE (отдельный гейт). По умолчанию COMBINED")
        AttendanceMode attendanceMode,

        @Schema(description = "SEPARATE: чем меряется гейт — PERCENT или COUNT")
        AttendanceRequirementMode attendanceRequirementMode,

        @Schema(description = "SEPARATE + PERCENT: минимальный процент посещённых занятий (0..100)") @Min(0) @Max(100) Integer attendanceMinPercent,

        @Schema(description = "SEPARATE + COUNT: минимальное количество посещённых занятий") @Min(0) @Max(1_000_000) Integer attendanceMinCount,

        @Schema(description = "SEPARATE: засчитывать ли PRESENT как посещение")
        Boolean attendanceCountPresent,

        @Schema(description = "SEPARATE: засчитывать ли LATE как посещение")
        Boolean attendanceCountLate,

        @Schema(description = "SEPARATE: засчитывать ли ABSENT как посещение")
        Boolean attendanceCountAbsent,

        @Schema(description = "SEPARATE: засчитывать ли EXCUSED как посещение")
        Boolean attendanceCountExcused) {

    @Schema(hidden = true)
    @AssertTrue(message = "Список bands должен быть непустым, когда enabled = true") public boolean isBandsPresentWhenEnabled() {
        if (!Boolean.TRUE.equals(enabled)) {
            return true;
        }
        return bands != null && !bands.isEmpty();
    }

    @Schema(
            name = "FinalAssessmentBandRequest",
            description = "Банда: ярлык и условия его получения (minPoints, minPercent и/или requiredTasks, AND)")
    public record Band(
            @Schema(description = "ID банды (null при создании новой)")
            UUID id,

            @Schema(description = "Ярлык, напр. «5», «зачтено»", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank @Size(max = 50) String label,

            @Schema(
                    description = "Минимальный итоговый балл (включительно). null — без ограничения по баллам",
                    types = {"integer", "null"})
            @Min(0) @Max(1_000_000) Integer minPoints,

            @Schema(
                    description = "Минимальный процент (0..100) от максимально возможных баллов "
                            + "(включительно). Отдельное условие рядом с minPoints. null — без ограничения по проценту",
                    types = {"integer", "null"})
            @Min(0) @Max(100) Integer minPercent,

            @Schema(
                    description = "Минимум закрытых обязательных задач. null — без ограничения по задачам",
                    types = {"integer", "null"})
            @Min(0) @Max(1_000_000) Integer requiredTasks) {}
}
