package com.github.k1mb1.vkr_backend.student.internal.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "Student", description = "Информация о студенте")
public record StudentResponse(
    @Schema(
        description = "ID студента",
        example = "550e8400-e29b-41d4-a716-446655440002"
    )
    UUID id,
    @Schema(description = "Имя пользователя студента", example = "ivanov_ii")
    String username,
    @Schema(
        description = "ID группы",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID groupId,
    @Schema(
        description = "ID подгруппы",
        example = "550e8400-e29b-41d4-a716-446655440001"
    )
    UUID subgroupId,
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
