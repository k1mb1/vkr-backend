package com.github.k1mb1.vkr_backend.subject.web.responses;

import com.github.k1mb1.vkr_backend.subject.domain.BonusOperation;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Параметры понижения за просрочку и бонуса за раннюю сдачу (расчёт на фронте)")
public record PenaltyPolicyResponse(
    @Schema(description = "Включена ли фича понижения балла")
    boolean enabled,

    @Schema(description = "Операция понижения: SUBTRACT или MULTIPLY")
    PenaltyOperation operation,

    @Schema(description = "Шаг понижения: вычитаемые баллы или множитель")
    BigDecimal step,

    @Schema(description = "Через сколько занятий после активного начинается первое понижение")
    Integer gracePeriodLessons,

    @Schema(description = "Через сколько занятий применяется каждое следующее понижение")
    Integer intervalLessons,

    @Schema(description = "Максимальное число понижений")
    Integer maxReductions,

    @Schema(description = "Включён ли бонус за раннюю сдачу")
    boolean bonusEnabled,

    @Schema(description = "Операция бонуса: ADD или MULTIPLY")
    BonusOperation bonusOperation,

    @Schema(description = "Шаг бонуса: прибавляемые баллы или множитель")
    BigDecimal bonusStep,

    @Schema(description = "Насколько занятий раньше срока нужно сдать, чтобы пошёл бонус")
    Integer bonusGracePeriodLessons,

    @Schema(description = "Через сколько занятий раньше применяется каждый следующий бонус")
    Integer bonusIntervalLessons,

    @Schema(description = "Максимальное число бонусов")
    Integer bonusMaxIncreases
) {}
