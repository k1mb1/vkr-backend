package com.github.k1mb1.vkr_backend.subject.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Информация о предмете")
public record SubjectResponse(
        @Schema(description = "ID предмета") UUID id,

        @Schema(description = "Название предмета") String name,

        @Schema(description = "Описание предмета") String description,

        @Schema(description = "Дата создания") Instant createdAt,

        @Schema(description = "Дата последнего обновления") Instant updatedAt) {}
