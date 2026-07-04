package com.github.k1mb1.vkr_backend.teacher.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска учителей")
public record TeacherFilter(
        @Schema(description = "Имя учителя (частичное совпадение)")
        String username) {}
