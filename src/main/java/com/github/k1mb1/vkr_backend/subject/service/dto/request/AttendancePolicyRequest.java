package com.github.k1mb1.vkr_backend.subject.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Schema(description = """
        Связка посещаемости с баллом: сколько баллов даёт каждый статус.
        Если enabled = true — коэффициенты обязательны (проверяется валидатором ниже, не только
        на сервисе). Коэффициенты могут быть отрицательными (штраф). Вклад в итог считает фронт.
        """)
public record AttendancePolicyRequest(
        @Schema(description = "Учитывается ли посещаемость в балле", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean enabled,

        @DecimalMin("-1000000") @DecimalMax("1000000") @Schema(description = "Баллы за присутствие (PRESENT)")
        BigDecimal pointsPresent,

        @DecimalMin("-1000000") @DecimalMax("1000000") @Schema(description = "Баллы за опоздание (LATE)")
        BigDecimal pointsLate,

        @DecimalMin("-1000000") @DecimalMax("1000000") @Schema(description = "Баллы за пропуск без уважительной причины (ABSENT), обычно отрицательные")
        BigDecimal pointsAbsent,

        @DecimalMin("-1000000") @DecimalMax("1000000") @Schema(description = "Баллы за пропуск по уважительной причине (EXCUSED)")
        BigDecimal pointsExcused) {

    @Schema(hidden = true)
    @AssertTrue(message = "Все коэффициенты баллов обязательны, когда enabled = true") public boolean isPointsPresentWhenEnabled() {
        if (!Boolean.TRUE.equals(enabled)) {
            return true;
        }
        return pointsPresent != null && pointsLate != null && pointsAbsent != null && pointsExcused != null;
    }
}
