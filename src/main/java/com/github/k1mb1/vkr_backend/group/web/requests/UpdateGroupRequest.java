package com.github.k1mb1.vkr_backend.group.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Запрос на частичное обновление группы")
public record UpdateGroupRequest(
    @Schema(description = "Новое название группы", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String groupName,

    @Schema(
        description = "Список студентов группы (полное обновление состава)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    List<@Valid StudentPatchRequest> students
) {
    @Builder
    @Schema(description = "Данные студента для обновления состава группы")
    public record StudentPatchRequest(
        @Schema(description = "ID существующего студента (null для нового)")
        UUID id,

        @Schema(
            description = "Имя пользователя студента", requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String username,

        @Schema(description = "ID подгруппы")
        UUID subgroupId
    ) {}
}
