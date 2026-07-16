package com.github.k1mb1.vkr_backend.journal.checkin.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Подтверждение кода аудитории перед доступом к поиску студентов")
public record VerifyCheckInCodeRequest(
        @NotBlank @Size(max = 32) @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Код аудитории содержит только латинские буквы и цифры") @Schema(description = "Код аудитории, показанный преподавателем", requiredMode = Schema.RequiredMode.REQUIRED)
        String code) {}
