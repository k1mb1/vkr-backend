package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Банда (полоса) итоговой аттестации: ярлык {@code label} и условия её получения. Едина для
 * зачёта и экзамена — зачёт это просто одна банда («зачтено»), экзамен — несколько («5», «4», «3»).
 * <p>
 * Условия комбинируются по AND (нужно выполнить все заданные); {@code null}-условие не ограничивает:
 * <pre>
 *   подходит = (minPoints == null   || total >= minPoints)
 *           && (minPercent == null  || total * 100 >= minPercent * maxPoints)
 *           && (requiredTasks == null || закрытоОбязательныхЗадач >= requiredTasks)
 * </pre>
 * {@code minPercent} — порог как процент от максимально возможных баллов ({@code maxPoints});
 * расчёт делает фронт.
 * Банда без условий — «пол» (подходит всегда). Банды хранятся в порядке убывания старшинства
 * через {@code position}; фронт выбирает первую подходящую.
 */
@Entity
@Table(name = "final_assessment_bands")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentBand extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    FinalAssessmentPolicy policy;

    @Column(name = "position", nullable = false)
    int position;

    /** Ярлык: «5», «4», «3», «зачтено», «автомат» — любой текст. */
    @Column(name = "label", nullable = false, length = 32)
    String label;

    /** Минимальный итоговый балл (включительно). null — балл не ограничивает банду. */
    @Column(name = "min_points")
    Integer minPoints;

    /**
     * Минимальный процент (0..100) от максимально возможных баллов предмета (включительно).
     * Отдельное условие рядом с {@code minPoints}. null — процент не ограничивает банду.
     */
    @Column(name = "min_percent")
    Integer minPercent;

    /** Минимум закрытых обязательных задач. null — задачи не ограничивают банду. */
    @Column(name = "required_tasks")
    Integer requiredTasks;
}
