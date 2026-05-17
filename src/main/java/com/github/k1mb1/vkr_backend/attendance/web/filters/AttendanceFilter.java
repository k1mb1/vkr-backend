package com.github.k1mb1.vkr_backend.attendance.web.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Фильтр для получения посещаемости")
public record AttendanceFilter(
    @NotNull
    @Schema(
        description = "ID права преподавателя на предмет",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID permissionId
) {}
