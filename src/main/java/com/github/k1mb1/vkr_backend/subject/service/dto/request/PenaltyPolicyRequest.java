package com.github.k1mb1.vkr_backend.subject.service.dto.request;

import com.github.k1mb1.vkr_backend.subject.BonusOperation;
import com.github.k1mb1.vkr_backend.subject.PenaltyOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
@Schema(description = """
        Параметры понижения балла за просрочку и бонуса за раннюю сдачу.
        Если enabled = true — поля понижения обязательны; если bonusEnabled = true — поля бонуса
        обязательны (проверяется валидатором ниже, а не только на сервисе). Все вычисления
        итогового балла делает фронт.
        """)
public record PenaltyPolicyRequest(
        @Schema(description = "Включена ли фича понижения балла", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull Boolean enabled,

        @Schema(description = "Операция понижения: SUBTRACT (вычитание) или MULTIPLY (умножение)")
        PenaltyOperation operation,

        @Schema(description = "Шаг понижения: вычитаемые баллы (SUBTRACT) либо множитель, напр. 0.5 (MULTIPLY)")
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("1000000") BigDecimal step,

        @Schema(description = "Через сколько занятий после активного начинается первое понижение") @Min(0) @Max(1_000) Integer gracePeriodLessons,

        @Schema(description = "Через сколько занятий применяется каждое следующее понижение") @Min(1) @Max(1_000) Integer intervalLessons,

        @Schema(description = "Максимальное число понижений") @Min(1) @Max(1_000) Integer maxReductions,

        @Schema(description = "Включён ли бонус за раннюю сдачу", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull Boolean bonusEnabled,

        @Schema(description = "Операция бонуса: ADD (прибавить) или MULTIPLY (умножить)")
        BonusOperation bonusOperation,

        @Schema(description = "Шаг бонуса: прибавляемые баллы (ADD) либо множитель, напр. 1.1 (MULTIPLY)")
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("1000000") BigDecimal bonusStep,

        @Schema(description = "Насколько занятий раньше срока нужно сдать, чтобы пошёл бонус") @Min(0) @Max(1_000) Integer bonusGracePeriodLessons,

        @Schema(description = "Через сколько занятий раньше применяется каждый следующий бонус") @Min(1) @Max(1_000) Integer bonusIntervalLessons,

        @Schema(description = "Максимальное число бонусов") @Min(1) @Max(1_000) Integer bonusMaxIncreases) {

    @Schema(hidden = true)
    @AssertTrue(message = "Поля понижения обязательны, когда enabled = true") public boolean isPenaltyFieldsPresentWhenEnabled() {
        if (!Boolean.TRUE.equals(enabled)) {
            return true;
        }
        return operation != null
                && step != null
                && gracePeriodLessons != null
                && intervalLessons != null
                && maxReductions != null;
    }

    @Schema(hidden = true)
    @AssertTrue(message = "Поля бонуса обязательны, когда bonusEnabled = true") public boolean isBonusFieldsPresentWhenEnabled() {
        if (!Boolean.TRUE.equals(bonusEnabled)) {
            return true;
        }
        return bonusOperation != null
                && bonusStep != null
                && bonusGracePeriodLessons != null
                && bonusIntervalLessons != null
                && bonusMaxIncreases != null;
    }
}
