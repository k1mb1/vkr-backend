package com.github.k1mb1.vkr_backend.lesson.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска занятий")
public record LessonFilter(
    @NotNull
    @Schema(
        description = "ID предмета",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID subjectId,

    @NotNull
    @Schema(
        description = "ID учителя",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID teacherId
) {}
