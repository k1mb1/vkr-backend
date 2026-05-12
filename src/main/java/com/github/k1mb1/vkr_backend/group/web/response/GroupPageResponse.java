package com.github.k1mb1.vkr_backend.group.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "GroupPage", description = "Краткая информация о группе (для списка)")
public record GroupPageResponse(
    @Schema(
        description = "ID группы",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID id,
    @Schema(description = "Название группы", example = "ИС-101") String name,
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
