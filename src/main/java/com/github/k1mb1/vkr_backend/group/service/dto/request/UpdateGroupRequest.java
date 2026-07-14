package com.github.k1mb1.vkr_backend.group.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = """
        Запрос на частичное обновление группы (PATCH).
        Семантика: поле = null или отсутствует — значение не изменяется.
        Особый случай: students = [] трактуется как "архивировать всех существующих".
        """)
public record UpdateGroupRequest(
        @Size(max = 200) @Schema(description = "Новое название группы (null — не менять)")
        String name,

        @Schema(
                description =
                        "Полный новый состав студентов. null — не трогать состав, []  — архивировать всех существующих",
                types = {"array", "null"})
        @Valid @Size(max = 2_000) List<@Valid StudentPatchRequest> students) {
    @Builder
    @Schema(name = "UpdateGroupStudentItem", description = "Данные студента для обновления состава группы")
    public record StudentPatchRequest(
            @Schema(description = "ID существующего студента (null для нового)")
            UUID id,

            @Schema(description = "Имя пользователя студента", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank @Size(max = 200) String username,

            @Schema(description = "ID подгруппы") UUID subgroupId) {}
}
