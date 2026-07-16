package com.github.k1mb1.vkr_backend.group.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Информация о студенте")
public record StudentResponse(
        @Schema(description = "ID студента") UUID id,

        @Schema(description = "Имя пользователя студента") String username,

        @Schema(description = "ID группы") UUID groupId,

        @Schema(description = "ID подгруппы") UUID subgroupId,

        @Schema(description = "Дата создания") Instant createdAt,

        @Schema(description = "Дата последнего обновления") Instant updatedAt) {}
