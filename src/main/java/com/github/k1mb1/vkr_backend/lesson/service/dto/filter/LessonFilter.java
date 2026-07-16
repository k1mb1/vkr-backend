package com.github.k1mb1.vkr_backend.lesson.service.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Фильтр для поиска занятий")
public record LessonFilter(
        @Schema(
                description = "ID разрешения преподавателя. Вернёт занятия по предмету, у которых либо allGroups=true, "
                        + "либо есть scope, попадающий хотя бы в один scope разрешения.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID permissionId) {}
