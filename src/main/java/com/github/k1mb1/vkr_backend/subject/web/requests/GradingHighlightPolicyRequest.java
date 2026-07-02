package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(
    description = """
        Цветовая подсветка ячеек оценок.
        Если enabled = false — подсветка не применяется. Цвета задаются в формате HEX (#RRGGBB).
        Раскраску ячеек выполняет фронт.
        """
)
public record GradingHighlightPolicyRequest(
    @Schema(
        description = "Включена ли подсветка оценок",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    Boolean enabled,

    @Schema(description = "Цвет ячейки с оценкой за задание в формате #RRGGBB", example = "#4CAF50")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String assignmentColor,

    @Schema(description = "Цвет ячейки с полным баллом в формате #RRGGBB", example = "#2E7D32")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String fullColor,

    @Schema(description = "Цвет ячейки с низким частичным баллом в формате #RRGGBB", example = "#FF9800")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String partialLowColor,

    @Schema(description = "Цвет ячейки с высоким частичным баллом в формате #RRGGBB", example = "#CDDC39")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    String partialHighColor
) {}
