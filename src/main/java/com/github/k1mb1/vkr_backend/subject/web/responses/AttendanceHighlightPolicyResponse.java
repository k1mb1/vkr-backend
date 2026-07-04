package com.github.k1mb1.vkr_backend.subject.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Цветовая подсветка таблицы посещаемости (раскраску ячеек выполняет фронт)")
public record AttendanceHighlightPolicyResponse(
        @Schema(description = "Применяется ли цветовая подсветка таблицы посещаемости")
        boolean enabled,

        @Schema(description = "Цвет ячейки со статусом PRESENT, HEX вида #00C16A")
        String presentColor,

        @Schema(description = "Цвет ячейки со статусом LATE, HEX вида #00C16A")
        String lateColor,

        @Schema(description = "Цвет ячейки со статусом ABSENT, HEX вида #00C16A")
        String absentColor,

        @Schema(description = "Цвет ячейки со статусом EXCUSED, HEX вида #00C16A")
        String excusedColor) {}
