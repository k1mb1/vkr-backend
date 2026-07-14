package com.github.k1mb1.vkr_backend.journal.service.dto.response;

import com.github.k1mb1.vkr_backend.subject.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Колонка таблицы посещаемости — конкретное проведение (lesson_scope)")
public record AttendanceTableLessonResponse(
        @Schema(description = "ID проведения (lesson_scope)")
        UUID id,

        @Schema(description = "ID шаблона занятия") UUID lessonId,

        @Schema(description = "Дата проведения") LocalDate startedAt,

        @Schema(description = "Тип занятия") LessonType type,

        @Schema(description = "Порядковый номер занятия в рамках (предмет, тип)")
        int orderIndex,

        @Schema(description = "Тема") String topic,

        @Schema(
                description =
                        "Активное (текущее) занятие — точка отсчёта для понижения/бонуса. Не более одного на (предмет, тип)")
        boolean active,

        @Schema(description = "ID группы (null = все группы)")
        UUID groupId,

        @Schema(description = "Название группы (null = все группы)")
        String groupName,

        @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
        UUID allowedSubgroupId,

        @Schema(description = "Индекс разрешённой подгруппы (null = вся группа)")
        Integer allowedSubgroupIndex,

        @Schema(description = "true = проведение для всех групп")
        boolean allGroups) {}
