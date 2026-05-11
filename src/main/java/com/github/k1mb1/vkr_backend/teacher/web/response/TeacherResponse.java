package com.github.k1mb1.vkr_backend.teacher.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Информация о преподавателе")
public record TeacherResponse(
    @Schema(
        description = "ID преподавателя",
        example = "550e8400-e29b-41d4-a716-446655440005"
    )
    UUID id,
    @Schema(
        description = "Имя пользователя преподавателя",
        example = "petrov_pp"
    )
    String username,
    @Schema(
        description = "Email преподавателя",
        example = "petrov@university.ru"
    )
    String email,
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
