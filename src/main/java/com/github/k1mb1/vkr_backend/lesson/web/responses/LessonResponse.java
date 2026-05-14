package com.github.k1mb1.vkr_backend.lesson.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Информация о занятии")
public record LessonResponse(
    @Schema(description = "ID занятия") UUID id,

    @Schema(description = "ID предмета") UUID subjectId,

    @Schema(description = "ID группы") UUID groupId,

    @Schema(description = "ID подгруппы (null для всей группы)")
    UUID subgroupId,

    @Schema(description = "Тип занятия") LessonType type,

    @Schema(description = "ID преподавателя") UUID teacherId,

    @Schema(description = "Время начала занятия") Instant startedAt,

    @Schema(description = "Время окончания занятия") Instant endedAt,

    @Schema(description = "Тема занятия") String topic,

    @Schema(description = "Дата создания") Instant createdAt,

    @Schema(description = "Дата последнего обновления") Instant updatedAt
) {}
