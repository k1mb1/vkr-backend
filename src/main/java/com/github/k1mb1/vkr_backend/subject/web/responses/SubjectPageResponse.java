package com.github.k1mb1.vkr_backend.subject.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Краткая информация о предмете (для списка)")
public record SubjectPageResponse(
    @Schema(description = "ID предмета")
    UUID id,

    @Schema(description = "Название предмета")
    String name,

    @Schema(description = "Описание предмета")
    String description,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
