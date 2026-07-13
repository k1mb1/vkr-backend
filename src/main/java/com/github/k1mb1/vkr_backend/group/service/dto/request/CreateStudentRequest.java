package com.github.k1mb1.vkr_backend.group.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
@Schema(description = "Запрос на создание студента")
public record CreateStudentRequest(
        @Schema(description = "Имя пользователя студента", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String username,

        @Schema(description = "ID группы") UUID groupId,

        @Schema(description = "ID подгруппы") @Nullable UUID subgroupId) {}
