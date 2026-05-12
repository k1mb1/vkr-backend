package com.github.k1mb1.vkr_backend.group.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(
    name = "StudentGroupMember",
    description = "Данные студента при создании группы"
)
public record StudentGroupMemberRequest(
    @Schema(
        description = "Имя пользователя студента",
        example = "ivanov_ii",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String username,
    @Schema(
        description = "Индекс подгруппы (0, 1, ...)",
        example = "0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Min(0)
    @Nullable
    Integer subgroupIndex
) {}
