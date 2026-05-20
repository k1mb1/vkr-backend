package com.github.k1mb1.vkr_backend.grade.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Таблица оценок: студенты × задания с существующими ячейками")
public record GradeTableResponse(
    @Schema(description = "Аудитория таблицы — группы и подгруппы, покрываемые разрешением")
    List<GradeAudienceScope> audience,

    @Schema(description = "Строки таблицы — студенты")
    List<GradeTableStudent> students,

    @Schema(description = "Занятия, к которым относятся задания (для построения шапки таблицы)")
    List<GradeTableLesson> lessons,

    @Schema(description = "Колонки таблицы — задания")
    List<GradeTableColumn> columns,

    @Schema(description = "Проставленные ячейки; если ячейки нет — оценка ещё не выставлена")
    List<GradeCellResponse> grades
) {}
