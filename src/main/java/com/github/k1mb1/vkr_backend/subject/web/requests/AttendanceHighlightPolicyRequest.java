package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(
    description = """
        Цветовая подсветка ячеек посещаемости.
        Если enabled = false — подсветка не применяется. Цвета задаются в формате HEX (#RRGGBB).
        Раскраску ячеек выполняет фронт.
        """
)
public record AttendanceHighlightPolicyRequest(
    @Schema(
        description = "Включена ли подсветка посещаемости",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    Boolean enabled,

    @Schema(description = "Цвет статуса PRESENT в формате #RRGGBB", example = "#4CAF50")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String presentColor,

    @Schema(description = "Цвет статуса LATE в формате #RRGGBB", example = "#FFC107")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String lateColor,

    @Schema(description = "Цвет статуса ABSENT в формате #RRGGBB", example = "#F44336")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String absentColor,

    @Schema(description = "Цвет статуса EXCUSED в формате #RRGGBB", example = "#2196F3")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String excusedColor
) {}
