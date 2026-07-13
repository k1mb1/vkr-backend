package com.github.k1mb1.vkr_backend.subject.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Schema(description = "Связка посещаемости с баллом (расчёт вклада выполняется на фронте)")
public record AttendancePolicyResponse(
        @Schema(description = "Учитывается ли посещаемость в балле")
        boolean enabled,

        @Schema(description = "Баллы за присутствие (PRESENT)")
        BigDecimal pointsPresent,

        @Schema(description = "Баллы за опоздание (LATE)") BigDecimal pointsLate,

        @Schema(description = "Баллы за пропуск без уважительной причины (ABSENT)")
        BigDecimal pointsAbsent,

        @Schema(description = "Баллы за пропуск по уважительной причине (EXCUSED)")
        BigDecimal pointsExcused) {}
