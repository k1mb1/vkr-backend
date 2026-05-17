package com.github.k1mb1.vkr_backend.lesson.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Информация о занятии")
public record LessonResponse(
    @Schema(description = "ID занятия")
    UUID id,

    @Schema(description = "ID предмета")
    UUID subjectId,

    @Schema(description = "Название предмета")
    String subjectName,

    @Schema(description = "ID группы")
    UUID groupId,

    @Schema(description = "Название группы")
    String groupName,

    @Schema(description = "ID подгруппы (null для всей группы)")
    UUID subgroupId,

    @Schema(description = "Индекс подгруппы (null для всей группы)")
    Integer subgroupIndex,

    @Schema(description = "Тип занятия")
    LessonType type,

    @Schema(description = "Дата занятия")
    LocalDate startedAt,

    @Schema(description = "Тема занятия")
    String topic,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
