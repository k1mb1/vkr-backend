package com.github.k1mb1.vkr_backend.student.internal.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Запрос на создание студента")
public record CreateStudentRequest(
    @Schema(
        description = "Имя пользователя студента", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String username,

    @Schema(description = "ID группы")
    UUID groupId,

    @Schema(description = "ID подгруппы")
    UUID subgroupId
) {}
