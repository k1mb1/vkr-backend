package com.github.k1mb1.vkr_backend.teacher.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Информация о преподавателе")
public record TeacherResponse(
    @Schema(description = "ID преподавателя")
    UUID id,

    @Schema(description = "Имя пользователя преподавателя")
    String username,

    @Schema(description = "Email преподавателя")
    String email,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
