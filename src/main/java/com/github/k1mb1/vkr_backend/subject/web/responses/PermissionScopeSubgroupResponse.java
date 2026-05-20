package com.github.k1mb1.vkr_backend.subject.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Краткая информация о подгруппе в составе scope")
public record PermissionScopeSubgroupResponse(
    @Schema(description = "ID подгруппы")
    UUID id,

    @Schema(description = "Индекс подгруппы")
    Integer index
) {}
