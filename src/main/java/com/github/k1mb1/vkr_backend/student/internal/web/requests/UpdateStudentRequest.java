package com.github.k1mb1.vkr_backend.student.internal.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "UpdateStudent", description = "Запрос на обновление студента")
public record UpdateStudentRequest(
    @Schema(
        description = "Имя пользователя студента",
        example = "ivanov_ii",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String username,
    @Schema(
        description = "ID подгруппы",
        example = "550e8400-e29b-41d4-a716-446655440001"
    )
    UUID subgroupId,
    @Schema(description = "Флаг архивации студента", example = "false")
    Boolean archived
) {}
