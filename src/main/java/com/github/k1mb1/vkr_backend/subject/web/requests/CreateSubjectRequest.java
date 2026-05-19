package com.github.k1mb1.vkr_backend.subject.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на создание предмета")
public record CreateSubjectRequest(
    @Schema(description = "Название предмета", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    String name,

    @Schema(description = "Описание предмета")
    String description,

    @Schema(
        description = "Список ID групп, к которым привязывается предмет (минимум одна группа)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    List<@NotNull UUID> groupIds,

    @Schema(description = "ID преподавателя", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    UUID teacherId
) {}
