package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Запрос на частичное обновление занятия")
public record UpdateLessonRequest(
    @Schema(description = "Тип занятия") LessonType type,
    @Schema(
        description = "Время начала занятия",
        example = "2024-01-15T09:00:00Z"
    )
    Instant startedAt,
    @Schema(
        description = "Время окончания занятия",
        example = "2024-01-15T10:30:00Z"
    )
    Instant endedAt,
    @Schema(description = "Тема занятия", example = "Введение в алгебру")
    String topic,
    @Schema(
        description = "ID преподавателя",
        example = "550e8400-e29b-41d4-a716-446655440005"
    )
    UUID teacherId,
    @Schema(
        description = "ID подгруппы (null для всей группы)",
        example = "550e8400-e29b-41d4-a716-446655440004"
    )
    UUID subgroupId
) {}
