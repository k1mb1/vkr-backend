package com.github.k1mb1.vkr_backend.subject.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Параметры понижения балла за просрочку и бонуса за раннюю сдачу, привязанные к предмету.
 * <p>
 * Бэкенд хранит конфигурацию и активное занятие по каждому типу (см. {@code Lesson.active},
 * одно на (предмет, тип)). Смещение сдачи {@code Grade.lessonsOffset} фиксируется один раз при
 * выставлении оценки с заданием: {@code активное.orderIndex - задание.orderIndex} по занятиям
 * того же типа (знаковое: &gt;0 — позже срока, &lt;0 — раньше). Хранится в строке оценки и
 * отдаётся в таблице как есть — корректно при любом фильтре. Фронт применяет политику:
 * <pre>
 *   offset = grade.lessonsOffset            // от бэкенда; null = ни штрафа, ни бонуса
 *   late   = max(0, offset)                 // на сколько занятий опоздал
 *   early  = max(0, -offset)                // на сколько занятий раньше сдал
 *
 *   // понижение
 *   if (enabled && offset != null && late &gt;= gracePeriodLessons):
 *     n = min(floor((late - gracePeriodLessons) / intervalLessons) + 1, maxReductions)
 *     SUBTRACT: score -= step * n ;  MULTIPLY: score *= step^n
 *
 *   // бонус
 *   if (bonusEnabled && offset != null && early &gt;= bonusGracePeriodLessons):
 *     n = min(floor((early - bonusGracePeriodLessons) / bonusIntervalLessons) + 1, bonusMaxIncreases)
 *     ADD: score += bonusStep * n ;  MULTIPLY: score *= bonusStep^n
 * </pre>
 */
@Hidden
@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyPolicy {

    /** Опциональная фича: включено ли понижение балла для предмета. */
    @Builder.Default
    @Column(name = "penalty_enabled", nullable = false)
    boolean enabled = false;

    /** Тип операции понижения. Заполняется только при enabled = true. */
    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_operation", length = 16)
    PenaltyOperation operation;

    /** Шаг понижения: вычитаемые баллы (SUBTRACT) либо множитель (MULTIPLY, напр. 0.5). */
    @Column(name = "penalty_step", precision = 6, scale = 3)
    BigDecimal step;

    /** Через сколько занятий после активного начинается первое понижение. */
    @Column(name = "penalty_grace_period_lessons")
    Integer gracePeriodLessons;

    /** Через сколько занятий применяется каждое следующее понижение. */
    @Column(name = "penalty_interval_lessons")
    Integer intervalLessons;

    /** Максимальное число понижений. */
    @Column(name = "penalty_max_reductions")
    Integer maxReductions;

    /** Опциональная фича: включён ли бонус за раннюю сдачу. */
    @Builder.Default
    @Column(name = "bonus_enabled", nullable = false)
    boolean bonusEnabled = false;

    /** Тип операции бонуса. Заполняется только при bonusEnabled = true. */
    @Enumerated(EnumType.STRING)
    @Column(name = "bonus_operation", length = 16)
    BonusOperation bonusOperation;

    /** Шаг бонуса: прибавляемые баллы (ADD) либо множитель (MULTIPLY, напр. 1.1). */
    @Column(name = "bonus_step", precision = 6, scale = 3)
    BigDecimal bonusStep;

    /** Насколько занятий раньше срока нужно сдать, чтобы пошёл бонус. */
    @Column(name = "bonus_grace_period_lessons")
    Integer bonusGracePeriodLessons;

    /** Через сколько занятий раньше применяется каждый следующий бонус. */
    @Column(name = "bonus_interval_lessons")
    Integer bonusIntervalLessons;

    /** Максимальное число бонусов. */
    @Column(name = "bonus_max_increases")
    Integer bonusMaxIncreases;
}
