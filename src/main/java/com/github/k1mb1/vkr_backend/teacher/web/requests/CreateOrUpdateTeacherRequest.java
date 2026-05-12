package com.github.k1mb1.vkr_backend.teacher.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(name = "CreateOrUpdateTeacher", description = "Запрос на создание или обновление преподавателя")
public record CreateOrUpdateTeacherRequest(
    @Schema(
        description = "Имя пользователя преподавателя",
        example = "petrov_pp",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String username,
    @Schema(
        description = "Email преподавателя",
        example = "petrov@university.ru",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    @Email
    String email
) {}
