package com.github.k1mb1.vkr_backend.student.internal.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Запрос на обновление студента")
public record UpdateStudentRequest(
    @Schema(
        description = "Имя пользователя студента", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String username,

    @Schema(description = "ID подгруппы")
    UUID subgroupId,

    @Schema(description = "Флаг архивации студента")
    Boolean archived
) {}
