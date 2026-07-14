package com.github.k1mb1.vkr_backend.subject.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на создание предмета")
public record CreateSubjectRequest(
        @Schema(description = "Название предмета", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 200) String name,

        @Size(max = 4_000) @Schema(description = "Описание предмета")
        String description,

        @Schema(
                description = "Список ID групп, к которым привязывается предмет (минимум одна группа)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Size(max = 200) List<@NotNull UUID> groupIds,

        @Schema(description = "ID преподавателя", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID teacherId) {}
