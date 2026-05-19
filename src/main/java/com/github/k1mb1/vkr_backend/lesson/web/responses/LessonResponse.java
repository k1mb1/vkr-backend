package com.github.k1mb1.vkr_backend.lesson.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Информация о занятии")
public record LessonResponse(
    @Schema(description = "ID занятия")
    UUID id,

    @Schema(description = "ID предмета")
    UUID subjectId,

    @Schema(description = "Название предмета")
    String subjectName,

    @Schema(description = "Тип занятия")
    LessonType type,

    @Schema(description = "Дата занятия")
    LocalDate startedAt,

    @Schema(description = "Тема занятия")
    String topic,

    @Schema(description = "true = занятие охватывает все группы предмета")
    boolean allGroups,

    @Schema(description = "Список scopes занятия (группа + опц. подгруппа)")
    List<LessonScopeResponse> scopes,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
