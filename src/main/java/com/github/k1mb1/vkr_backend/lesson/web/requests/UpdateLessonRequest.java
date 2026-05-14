package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Запрос на частичное обновление занятия")
public record UpdateLessonRequest(
    @Schema(description = "ID предмета")
    UUID subjectId,

    @Schema(description = "ID группы")
    UUID groupId,

    @Schema(description = "Тип занятия")
    LessonType type,

    @Schema(description = "Время начала занятия")
    Instant startedAt,

    @Schema(description = "Время окончания занятия")
    Instant endedAt,

    @Schema(description = "Тема занятия")
    String topic,

    @Schema(description = "ID преподавателя")
    UUID teacherId,

    @Schema(description = "ID подгруппы (null для всей группы)")
    UUID subgroupId
) {}
