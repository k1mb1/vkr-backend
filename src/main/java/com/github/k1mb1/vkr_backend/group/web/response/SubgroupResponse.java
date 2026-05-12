package com.github.k1mb1.vkr_backend.group.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Информация о подгруппе")
public record SubgroupResponse(
    @Schema(description = "ID подгруппы")
    UUID id,

    @Schema(description = "Индекс подгруппы")
    Integer index,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
