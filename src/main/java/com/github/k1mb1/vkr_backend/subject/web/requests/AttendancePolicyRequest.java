package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(
    description = """
        Связка посещаемости с баллом: сколько баллов даёт каждый статус.
        Если enabled = true — коэффициенты обязательны (проверяется на сервисе).
        Коэффициенты могут быть отрицательными (штраф). Вклад в итог считает фронт.
        """
)
public record AttendancePolicyRequest(
    @Schema(description = "Учитывается ли посещаемость в балле", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    Boolean enabled,

    @Schema(description = "Баллы за присутствие (PRESENT)")
    BigDecimal pointsPresent,

    @Schema(description = "Баллы за опоздание (LATE)")
    BigDecimal pointsLate,

    @Schema(description = "Баллы за пропуск без уважительной причины (ABSENT), обычно отрицательные")
    BigDecimal pointsAbsent,

    @Schema(description = "Баллы за пропуск по уважительной причине (EXCUSED)")
    BigDecimal pointsExcused
) {}
