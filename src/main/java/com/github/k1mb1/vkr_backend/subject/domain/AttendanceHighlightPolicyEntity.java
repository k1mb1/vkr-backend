package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Цветовая подсветка таблицы посещаемости (опциональная фича).
 * <p>
 * Бэкенд только хранит цвета и отдаёт их в таблице посещаемости — раскраску ячеек выполняет фронт.
 * Цвета хранятся в HEX-формате вида {@code #00C16A}.
 * <p>
 * Логика применения на фронте:
 * <pre>
 *   статус PRESENT   -> presentColor
 *   статус LATE      -> lateColor
 *   статус ABSENT    -> absentColor
 *   статус EXCUSED   -> excusedColor
 * </pre>
 * Если {@code enabled = false} — подсветку фронт не применяет.
 */
@Hidden
@Entity
@Table(name = "attendance_highlight_policies")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceHighlightPolicyEntity extends BaseEntity {

    /** Опциональная фича: применяется ли цветовая подсветка таблицы посещаемости. */
    @Builder.Default
    @Column(nullable = false)
    boolean enabled = false;

    /** Цвет ячейки со статусом PRESENT. HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String presentColor = "#DCFCE7";

    /** Цвет ячейки со статусом LATE. HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String lateColor = "#FEF3C7";

    /** Цвет ячейки со статусом ABSENT. HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String absentColor = "#FEE2E2";

    /** Цвет ячейки со статусом EXCUSED. HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String excusedColor = "#DBEAFE";
}
