package com.github.k1mb1.vkr_backend.group.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(
    description = """
        Запрос на частичное обновление группы (PATCH).
        Семантика: поле = null или отсутствует — значение не изменяется.
        Особый случай: students = [] трактуется как "архивировать всех существующих".
        """
)
public record UpdateGroupRequest(
    @Schema(description = "Новое название группы (null — не менять)")
    String name,

    @Schema(
        description = "Полный новый состав студентов. null — не трогать состав, []  — архивировать всех существующих",
        types = {"array", "null"}
    )
    List<@Valid StudentPatchRequest> students
) {
    @Builder
    @Schema(name = "UpdateGroupStudentItem", description = "Данные студента для обновления состава группы")
    public record StudentPatchRequest(
        @Schema(
            description = "ID существующего студента (null для нового)"
        )
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
