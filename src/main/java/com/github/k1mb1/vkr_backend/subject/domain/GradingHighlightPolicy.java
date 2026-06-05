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
 * Цветовая подсветка таблицы оценок (опциональная фича). Бэкенд только хранит цвета и отдаёт их
 * в таблице оценок — раскраску ячеек выполняет фронт.
 * <p>
 * Цвета хранятся в HEX-формате вида {@code #00C16A}. Логика применения на фронте:
 * <pre>
 *   колонки заданий            -> assignmentColor   (голубой)
 *   набрано 100% от max        -> fullColor          (жёлтый)
 *   набрано <= 50% от max       -> partialLowColor    (светло-жёлтый)
 *   набрано > 50% и < 100%      -> partialHighColor   (потемнее)
 * </pre>
 * Признак штрафа/бонуса фронт вычисляет сам по {@code grade.lessonsOffset} и PenaltyPolicy;
 * подсветка лишь даёт цвета. Если {@code enabled = false} — подсветку фронт не применяет.
 */
@Hidden
@Entity
@Table(name = "grading_highlight_policies")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GradingHighlightPolicy extends BaseEntity {

    /** Опциональная фича: применяется ли цветовая подсветка таблицы оценок. */
    @Builder.Default
    @Column(nullable = false)
    boolean enabled = false;

    /** Цвет колонок с заданиями на л.р. (голубой). HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String assignmentColor = "#B3E5FC";

    /** Цвет полностью решённой л.р. (жёлтый). HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String fullColor = "#FFEB3B";

    /** Цвет л.р., решённой не более чем на половину (светло-жёлтый). HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String partialLowColor = "#FFF9C4";

    /** Цвет л.р., решённой более чем на половину (потемнее). HEX вида {@code #00C16A}. */
    @Builder.Default
    @Column(nullable = false, length = 7)
    String partialHighColor = "#FFF176";
}
