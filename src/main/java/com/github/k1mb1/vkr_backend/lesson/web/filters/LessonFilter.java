package com.github.k1mb1.vkr_backend.lesson.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Фильтр для поиска занятий")
public record LessonFilter(
    @Schema(description = "ID предмета")
    UUID subjectId
) {}
