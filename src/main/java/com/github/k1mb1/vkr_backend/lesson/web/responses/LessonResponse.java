package com.github.k1mb1.vkr_backend.lesson.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "Lesson", description = "Информация о занятии")
public record LessonResponse(
    @Schema(
        description = "ID занятия",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID id,
    @Schema(
        description = "ID назначения предмета (offering)",
        example = "550e8400-e29b-41d4-a716-446655440001"
    )
    UUID offeringId,
    @Schema(
        description = "ID предмета",
        example = "550e8400-e29b-41d4-a716-446655440002"
    )
    UUID subjectId,
    @Schema(
        description = "ID группы",
        example = "550e8400-e29b-41d4-a716-446655440003"
    )
    UUID groupId,
    @Schema(
        description = "ID подгруппы (null для всей группы)",
        example = "550e8400-e29b-41d4-a716-446655440004"
    )
    UUID subgroupId,
    @Schema(description = "Тип занятия") LessonType type,
    @Schema(
        description = "ID преподавателя",
        example = "550e8400-e29b-41d4-a716-446655440005"
    )
    UUID teacherId,
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
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
