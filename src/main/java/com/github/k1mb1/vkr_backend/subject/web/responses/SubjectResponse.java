package com.github.k1mb1.vkr_backend.subject.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Информация о предмете")
public record SubjectResponse(
    @Schema(
        description = "ID предмета",
        example = "550e8400-e29b-41d4-a716-446655440002"
    )
    UUID id,
    @Schema(
        description = "Название предмета",
        example = "Математический анализ"
    )
    String name,
    @Schema(
        description = "Описание предмета",
        example = "Базовый курс математического анализа"
    )
    String description,
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
