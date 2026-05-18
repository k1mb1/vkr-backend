package com.github.k1mb1.vkr_backend.attendance.checkin.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Отметка студента в check-in сессии")
public record StudentCheckInRequest(
    @NotNull
    @Schema(
        description = "ID студента", requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID studentId
) {}
