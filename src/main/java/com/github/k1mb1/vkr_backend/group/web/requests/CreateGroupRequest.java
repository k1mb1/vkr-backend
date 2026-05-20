package com.github.k1mb1.vkr_backend.group.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Запрос на создание группы со списком студентов")
public record CreateGroupRequest(
    @Schema(
        description = "Название группы", requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String name,

    @Schema(
        description = "Список студентов для добавления в группу",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    List<@Valid StudentGroupMemberRequest> students
) {}
