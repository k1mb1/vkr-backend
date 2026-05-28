package com.github.k1mb1.vkr_backend.group.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "Группа с подгруппами (без студентов)")
public record GroupWithSubgroupsResponse(
    @Schema(description = "ID группы")
    UUID id,

    @Schema(description = "Название группы")
    String name,

    @Schema(description = "Список подгрупп")
    List<SubgroupResponse> subgroups
) {}
