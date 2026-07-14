package com.github.k1mb1.vkr_backend.journal.checkin.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
@Schema(description = "Одна аудиторная единица опроса — группа и опционально подгруппа")
public record CheckInAudienceScopeResponse(
        @Schema(description = "ID группы") UUID groupId,

        @Schema(description = "Название группы") String groupName,

        @Schema(description = "ID подгруппы (null = вся группа)") @Nullable UUID allowedSubgroupId,

        @Schema(description = "Индекс подгруппы (null = вся группа)") @Nullable Integer allowedSubgroupIndex) {}
