package com.github.k1mb1.vkr_backend.grade.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Колонка таблицы оценок — задание с контекстом занятия")
public record GradeTableColumn(
    @Schema(description = "ID задания") UUID id,
    @Schema(description = "Название задания") String title,
    @Schema(description = "Обязательное") boolean required,
    @Schema(description = "Максимальный балл") int maxScore,
    @Schema(description = "ID занятия") UUID lessonId,
    @Schema(description = "Дата занятия") LocalDate lessonDate,
    @Schema(description = "Тип занятия") LessonType lessonType
) {}
