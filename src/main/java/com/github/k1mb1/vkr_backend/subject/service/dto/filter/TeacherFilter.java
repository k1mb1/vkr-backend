package com.github.k1mb1.vkr_backend.subject.service.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска учителей")
public record TeacherFilter(
        @Size(max = 200) @Schema(description = "Имя учителя (частичное совпадение)")
        String username) {}
