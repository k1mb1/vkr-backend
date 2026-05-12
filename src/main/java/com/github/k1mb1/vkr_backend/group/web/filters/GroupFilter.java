package com.github.k1mb1.vkr_backend.group.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска групп")
public record GroupFilter(
    @Schema(description = "Название группы (частичное совпадение)")
    String name
) {}
