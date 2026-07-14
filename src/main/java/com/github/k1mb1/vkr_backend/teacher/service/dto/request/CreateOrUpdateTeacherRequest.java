package com.github.k1mb1.vkr_backend.teacher.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на создание или обновление преподавателя")
public record CreateOrUpdateTeacherRequest(
        @Schema(description = "Имя пользователя преподавателя", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 200) String username,

        @Schema(description = "Email преподавателя", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email @Size(max = 254) String email) {}
