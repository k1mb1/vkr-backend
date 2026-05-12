package com.github.k1mb1.vkr_backend.group.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(name = "CreateGroup", description = "Запрос на создание группы со списком студентов")
public record CreateGroupRequest(
    @Schema(
        description = "Название группы",
        example = "ИС-101",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    String groupName,
    @Schema(
        description = "Список студентов для добавления в группу",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    List<@Valid StudentGroupMemberRequest> students
) {}
