package com.github.k1mb1.vkr_backend.grade.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Занятие в таблице оценок")
public record GradeTableLesson(
    @Schema(description = "ID занятия") UUID id,

    @Schema(description = "Дата занятия") LocalDate startedAt,

    @Schema(description = "Тип занятия") LessonType type,

    @Schema(description = "Тема") String topic
) {}
