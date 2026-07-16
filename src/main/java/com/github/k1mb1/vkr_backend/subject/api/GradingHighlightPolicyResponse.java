package com.github.k1mb1.vkr_backend.subject.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Цветовая подсветка таблицы оценок (раскраску ячеек выполняет фронт)")
public record GradingHighlightPolicyResponse(
        @Schema(description = "Применяется ли цветовая подсветка таблицы оценок")
        boolean enabled,

        @Schema(description = "Цвет колонок с заданиями на л.р. (голубой), HEX вида #00C16A")
        String assignmentColor,

        @Schema(description = "Цвет полностью решённой л.р. (жёлтый), HEX вида #00C16A")
        String fullColor,

        @Schema(description = "Цвет л.р., решённой не более чем на половину (светло-жёлтый), HEX вида #00C16A")
        String partialLowColor,

        @Schema(description = "Цвет л.р., решённой более чем на половину (потемнее), HEX вида #00C16A")
        String partialHighColor) {}
