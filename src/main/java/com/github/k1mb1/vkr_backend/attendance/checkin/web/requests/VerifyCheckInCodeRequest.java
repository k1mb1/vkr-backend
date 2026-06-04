package com.github.k1mb1.vkr_backend.attendance.checkin.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Подтверждение кода аудитории перед доступом к поиску студентов")
public record VerifyCheckInCodeRequest(
    @NotBlank
    @Schema(
        description = "Код аудитории, показанный преподавателем",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code
) {}
