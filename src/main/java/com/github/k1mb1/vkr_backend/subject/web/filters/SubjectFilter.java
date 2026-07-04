package com.github.k1mb1.vkr_backend.subject.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска предметов")
public record SubjectFilter(
        @Schema(description = "Название предмета (частичное совпадение)")
        String name,

        @Schema(description = "ID учителя", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID teacherId) {}
