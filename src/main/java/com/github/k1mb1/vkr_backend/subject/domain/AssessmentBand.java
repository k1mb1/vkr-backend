package com.github.k1mb1.vkr_backend.subject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Банда (полоса) итоговой аттестации: ярлык {@code label} и условия его получения. Едина для
 * зачёта и экзамена — зачёт это просто одна банда («зачтено»), экзамен — несколько («5», «4», «3»).
 * <p>
 * Условия комбинируются по AND (нужно выполнить все заданные); {@code null}-условие не ограничивает:
 * <pre>
 *   подходит = (minPoints == null || total >= minPoints)
 *           && (requiredTasks == null || закрытоОбязательныхЗадач >= requiredTasks)
 * </pre>
 * Банда без условий — «пол» (подходит всегда). Банды хранятся в порядке убывания старшинства;
 * фронт выбирает первую подходящую.
 */
@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentBand {

    /** Ярлык: «5», «4», «3», «зачтено», «автомат» — любой текст. */
    @Column(name = "label", nullable = false, length = 32)
    String label;

    /** Минимальный итоговый балл (включительно). null — балл не ограничивает банду. */
    @Column(name = "min_points")
    Integer minPoints;

    /** Минимум закрытых обязательных задач. null — задачи не ограничивают банду. */
    @Column(name = "required_tasks")
    Integer requiredTasks;
}
