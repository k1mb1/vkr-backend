package com.github.k1mb1.vkr_backend.group.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Данные студента при создании группы")
public record StudentGroupMemberRequest(
        @Schema(description = "Имя пользователя студента", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 200) String username,

        @Schema(description = "Индекс подгруппы (1, 2, ...)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Min(1) @Nullable Integer subgroupIndex) {}
