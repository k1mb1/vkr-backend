package com.github.k1mb1.vkr_backend.group.service.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска групп")
public record GroupFilter(
        @Size(max = 200) @Schema(description = "Название группы (частичное совпадение)")
        String name) {}
