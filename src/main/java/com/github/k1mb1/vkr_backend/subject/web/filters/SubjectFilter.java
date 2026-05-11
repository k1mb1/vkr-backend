package com.github.k1mb1.vkr_backend.subject.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска предметов")
public record SubjectFilter(
    @Schema(
        description = "Название предмета (частичное совпадение)",
        example = "Математика"
    )
    String name
) {}
