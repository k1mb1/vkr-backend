package com.github.k1mb1.vkr_backend.lesson.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска занятий")
public record LessonFilter(
    @Schema(
        description = "ID предмета",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID subjectId
) {}
