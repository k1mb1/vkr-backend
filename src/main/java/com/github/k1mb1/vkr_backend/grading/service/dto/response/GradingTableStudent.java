package com.github.k1mb1.vkr_backend.grading.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Студент в таблице оценок")
public record GradingTableStudent(
        @Schema(description = "ID студента") UUID id,

        @Schema(description = "Имя студента") String username,

        @Schema(description = "ID группы студента") UUID groupId,

        @Schema(description = "Название группы студента") String groupName,

        @Schema(description = "ID подгруппы (null = без подгруппы)")
        UUID subgroupId,

        @Schema(description = "Индекс подгруппы (null = без подгруппы)")
        Integer subgroupIndex) {}
