package com.github.k1mb1.vkr_backend.grade.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Студент в таблице оценок")
public record GradeTableStudent(
    @Schema(description = "ID студента") UUID id,
    @Schema(description = "Имя студента") String username,
    @Schema(description = "ID группы") UUID groupId,
    @Schema(description = "Название группы") String groupName,
    @Schema(description = "ID подгруппы (null = без подгруппы)") UUID subgroupId,
    @Schema(description = "Индекс подгруппы (null = без подгруппы)") Integer subgroupIndex
) {}
