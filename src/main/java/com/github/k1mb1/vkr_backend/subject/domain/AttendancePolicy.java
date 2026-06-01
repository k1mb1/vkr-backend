package com.github.k1mb1.vkr_backend.subject.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Связка посещаемости с баллом: сколько баллов даёт каждый статус посещаемости.
 * <p>
 * Бэкенд только хранит коэффициенты и отдаёт в таблице оценок сводку посещаемости по студенту
 * (кол-во по каждому статусу). Вклад в итог считает фронт:
 * <pre>
 *   if (!enabled) -&gt; вклада нет
 *   attendancePoints = present  * pointsPresent
 *                    + late     * pointsLate
 *                    + absent   * pointsAbsent
 *                    + excused  * pointsExcused
 *   total = сумма_баллов_за_задания + attendancePoints
 * </pre>
 * Коэффициенты могут быть отрицательными (штраф за пропуск) или нулевыми.
 */
@Hidden
@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicy {

    /** Опциональная фича: учитывается ли посещаемость в балле. */
    @Builder.Default
    @Column(name = "attendance_enabled", nullable = false)
    boolean enabled = false;

    /** Баллы за присутствие (PRESENT). */
    @Builder.Default
    @Column(name = "attendance_points_present", nullable = false, precision = 6, scale = 3)
    BigDecimal pointsPresent = BigDecimal.ZERO;

    /** Баллы за опоздание (LATE). */
    @Builder.Default
    @Column(name = "attendance_points_late", nullable = false, precision = 6, scale = 3)
    BigDecimal pointsLate = BigDecimal.ZERO;

    /** Баллы за пропуск без уважительной причины (ABSENT), обычно отрицательные. */
    @Builder.Default
    @Column(name = "attendance_points_absent", nullable = false, precision = 6, scale = 3)
    BigDecimal pointsAbsent = BigDecimal.ZERO;

    /** Баллы за пропуск по уважительной причине (EXCUSED). */
    @Builder.Default
    @Column(name = "attendance_points_excused", nullable = false, precision = 6, scale = 3)
    BigDecimal pointsExcused = BigDecimal.ZERO;
}
