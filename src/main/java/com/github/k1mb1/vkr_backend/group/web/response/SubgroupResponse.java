package com.github.k1mb1.vkr_backend.group.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "Subgroup", description = "Информация о подгруппе")
public record SubgroupResponse(
    @Schema(
        description = "ID подгруппы",
        example = "550e8400-e29b-41d4-a716-446655440001"
    )
    UUID id,
    @Schema(description = "Индекс подгруппы", example = "0") Integer index,
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
