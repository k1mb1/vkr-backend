package com.github.k1mb1.vkr_backend.subject.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Группа в составе scope с полным списком её подгрупп")
public record PermissionScopeGroupResponse(
        @Schema(description = "ID группы") UUID id,

        @Schema(description = "Название группы") String name,

        @Schema(description = "Все подгруппы этой группы") List<PermissionScopeSubgroupResponse> subgroups) {}
