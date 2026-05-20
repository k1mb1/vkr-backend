package com.github.k1mb1.vkr_backend.grading.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    description = "Таблица оценок: студенты × (задания + extra-колонка) с существующими ячейками"
)
public record GradingTableResponse(
    @Schema(
        description = "Аудитория таблицы — группы и подгруппы, покрываемые разрешением " + "(для построения заголовка таблицы)."
    )
    List<GradingAudienceScope> audience,

    @Schema(description = "Строки таблицы — студенты")
    List<GradingTableStudent> students,

    @Schema(description = "Занятия (нужны для построения подзаголовков колонок и extra-оценок по уроку)")
    List<GradingTableLesson> lessons,

    @Schema(description = "Задания (колонки таблицы внутри каждого урока)")
    List<AssignmentResponse> assignments,

    @Schema(description = "Проставленные ячейки оценок (включая оценки вне заданий — у них assignmentId=null)")
    List<GradeCellResponse> grades
) {}
