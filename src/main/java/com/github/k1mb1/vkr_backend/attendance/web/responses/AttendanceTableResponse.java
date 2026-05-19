package com.github.k1mb1.vkr_backend.attendance.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    description = "Таблица посещаемости: студенты × занятия с существующими ячейками"
)
public record AttendanceTableResponse(
    @Schema(
        description = "Аудитория таблицы — группы и подгруппы, покрываемые разрешением " +
            "(для построения заголовка таблицы)."
    )
    List<AttendanceAudienceScope> audience,

    @Schema(description = "Строки таблицы — студенты")
    List<AttendanceTableStudent> students,

    @Schema(description = "Колонки таблицы — занятия")
    List<AttendanceTableLesson> lessons,

    @Schema(
        description = "Проставленные ячейки; если ячейки нет — отметка ещё не проставлена"
    )
    List<AttendanceCellResponse> attendances
) {}
