package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Связка посещаемости с баллом: сколько баллов даёт каждый статус посещаемости.
 * <p>
 * Бэкенд только хранит коэффициенты и отдаёт в таблице оценок сводку посещаемости по студенту
 * (кол-во по каждому статусу). Вклад в итог считает фронт:
 * <pre>
 *   if (!enabled) -> вклада нет
 *   attendancePoints = present  * pointsPresent
 *                    + late     * pointsLate
 *                    + absent   * pointsAbsent
 *                    + excused  * pointsExcused
 *   total = сумма_баллов_за_задания + attendancePoints
 * </pre>
 * Коэффициенты могут быть отрицательными (штраф за пропуск) или нулевыми.
 */
@Hidden
@Entity
@Table(name = "attendance_policies")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicyEntity extends BaseEntity {

    /** Опциональная фича: учитывается ли посещаемость в балле. */
    @Builder.Default
    @Column(nullable = false)
    boolean enabled = false;

    /** Баллы за присутствие (PRESENT). */
    @Builder.Default
    @Column(nullable = false, precision = 6, scale = 3)
    BigDecimal pointsPresent = BigDecimal.ZERO;

    /** Баллы за опоздание (LATE). */
    @Builder.Default
    @Column(nullable = false, precision = 6, scale = 3)
    BigDecimal pointsLate = BigDecimal.ZERO;

    /** Баллы за пропуск без уважительной причины (ABSENT), обычно отрицательные. */
    @Builder.Default
    @Column(nullable = false, precision = 6, scale = 3)
    BigDecimal pointsAbsent = BigDecimal.ZERO;

    /** Баллы за пропуск по уважительной причине (EXCUSED). */
    @Builder.Default
    @Column(nullable = false, precision = 6, scale = 3)
    BigDecimal pointsExcused = BigDecimal.ZERO;
}
