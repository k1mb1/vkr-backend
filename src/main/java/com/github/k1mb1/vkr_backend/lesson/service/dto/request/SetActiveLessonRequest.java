package com.github.k1mb1.vkr_backend.lesson.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = """
        Пометить занятие как активное (текущее) или снять отметку.
        active = true делает занятие активным и снимает флаг с остальных занятий того же типа.
        """)
public record SetActiveLessonRequest(
        @Schema(
                description = "true — сделать активным, false — снять отметку",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean active) {}
