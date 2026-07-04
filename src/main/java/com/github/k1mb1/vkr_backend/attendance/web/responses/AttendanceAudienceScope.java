package com.github.k1mb1.vkr_backend.attendance.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
@Schema(
        description =
                "Аудитория таблицы — одна запись на (группа, опц. подгруппа). "
                        + "Один scope разрешения = одна запись; при allPermissions=true — по одной записи на каждую группу предмета.")
public record AttendanceAudienceScope(
        @Schema(description = "ID группы") UUID groupId,

        @Schema(description = "Название группы") String groupName,

        @Schema(description = "ID разрешённой подгруппы (null = вся группа)") @Nullable UUID allowedSubgroupId,

        @Schema(description = "Индекс разрешённой подгруппы (null = вся группа)") @Nullable Integer allowedSubgroupIndex) {}
