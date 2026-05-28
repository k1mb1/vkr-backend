package com.github.k1mb1.vkr_backend.lesson.web.responses;

import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Информация о занятии (шаблон без даты — даты в scopes)")
public record LessonResponse(
    @Schema(description = "ID занятия")
    UUID id,

    @Schema(description = "ID предмета")
    UUID subjectId,

    @Schema(description = "Название предмета")
    String subjectName,

    @Schema(description = "Тип занятия")
    LessonType type,

    @Schema(description = "Порядковый номер занятия в рамках (предмет, тип)")
    int orderIndex,

    @Schema(description = "Тема занятия")
    String topic,

    @Schema(description = "Список проведений занятия (группа/подгруппа + дата)")
    List<LessonScopeResponse> scopes,

    @Schema(description = "Задания занятия, отсортированы по order (пустой список — заданий нет)")
    List<AssignmentResponse> assignments,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
