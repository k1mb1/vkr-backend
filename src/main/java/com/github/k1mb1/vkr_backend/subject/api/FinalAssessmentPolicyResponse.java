package com.github.k1mb1.vkr_backend.subject.api;

import com.github.k1mb1.vkr_backend.subject.AttendanceMode;
import com.github.k1mb1.vkr_backend.subject.AttendanceRequirementMode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Параметры промежуточной аттестации (итоги считает фронт)")
public record FinalAssessmentPolicyResponse(
        @Schema(description = "Включены ли итоги (банды)") boolean enabled,

        @Schema(description = "Банды по убыванию старшинства (первая — самая старшая)")
        List<Band> bands,

        @Schema(description = "Учёт посещаемости: COMBINED (в балл) или SEPARATE (отдельный гейт)")
        AttendanceMode attendanceMode,

        @Schema(description = "SEPARATE: чем меряется гейт — PERCENT или COUNT")
        AttendanceRequirementMode attendanceRequirementMode,

        @Schema(description = "SEPARATE + PERCENT: минимальный процент посещённых занятий (0..100)")
        Integer attendanceMinPercent,

        @Schema(description = "SEPARATE + COUNT: минимальное количество посещённых занятий")
        Integer attendanceMinCount,

        @Schema(description = "SEPARATE: засчитывается ли PRESENT как посещение")
        boolean attendanceCountPresent,

        @Schema(description = "SEPARATE: засчитывается ли LATE как посещение")
        boolean attendanceCountLate,

        @Schema(description = "SEPARATE: засчитывается ли ABSENT как посещение")
        boolean attendanceCountAbsent,

        @Schema(description = "SEPARATE: засчитывается ли EXCUSED как посещение")
        boolean attendanceCountExcused) {
    @Schema(
            name = "FinalAssessmentBandResponse",
            description = "Банда: ярлык и условия его получения (minPoints, minPercent и/или requiredTasks, AND)")
    @Builder
    public record Band(
            @Schema(description = "ID банды") UUID id,

            @Schema(description = "Ярлык, напр. «5», «зачтено»")
            String label,

            @Schema(description = "Минимальный итоговый балл (включительно), null — без ограничения")
            Integer minPoints,

            @Schema(
                    description = "Минимальный процент (0..100) от максимально возможных баллов "
                            + "(включительно), null — без ограничения")
            Integer minPercent,

            @Schema(description = "Минимум закрытых обязательных задач, null — без ограничения")
            Integer requiredTasks) {}
}
