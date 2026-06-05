package com.github.k1mb1.vkr_backend.subject.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = """
        Чем меряется отдельное требование по посещаемости (для AttendanceMode.SEPARATE).
        PERCENT — минимальный процент посещённых занятий (attendanceMinPercent, 0..100).
        COUNT — минимальное количество посещённых занятий (attendanceMinCount).
        «Посещённым» считается занятие со статусом из включённых attendanceCount* флагов.
        """
)
public enum AttendanceRequirementMode {
    PERCENT,
    COUNT,
}
