package com.github.k1mb1.vkr_backend.group.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Краткая информация о группе (для списка)")
public record GroupPageResponse(
    @Schema(description = "ID группы")
    UUID id,

    @Schema(description = "Название группы")
    String name,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
