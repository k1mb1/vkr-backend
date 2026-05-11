package com.github.k1mb1.vkr_backend.group.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на частичное обновление группы")
public record UpdateGroupRequest(
    @Schema(
        description = "Новое название группы",
        example = "ИС-102",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
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
        @Schema(
            description = "ID существующего студента (null для нового)",
            example = "550e8400-e29b-41d4-a716-446655440000"
        ) UUID id,
        @Schema(
            description = "Имя пользователя студента",
            example = "ivanov_ii",
            requiredMode = Schema.RequiredMode.REQUIRED
        ) @NotBlank String username,
        @Schema(
            description = "ID подгруппы",
            example = "550e8400-e29b-41d4-a716-446655440001"
        ) UUID subgroupId
    ) {}
}
