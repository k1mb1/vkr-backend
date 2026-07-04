package com.github.k1mb1.vkr_backend.attendance.web.responses;

import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Таблица посещаемости: студенты × занятия с существующими ячейками")
public record AttendanceTableResponse(
        @Schema(
                description = "Цветовая подсветка таблицы посещаемости. Если enabled=true — фронт раскрашивает "
                        + "ячейки по этим цветам (HEX вида #00C16A).")
        AttendanceHighlightPolicyResponse highlightPolicy,

        @Schema(
                description = "Аудитория таблицы — группы/подгруппы из scope'ов разрешения. "
                        + "Пустой список при allPermissions=true: клиент должен взять группы из subject.groups.")
        List<AttendanceAudienceScope> audience,

        @Schema(description = "Строки таблицы — студенты") List<AttendanceTableStudent> students,

        @Schema(description = "Колонки таблицы — занятия") List<AttendanceTableLesson> lessons,

        @Schema(description = "Проставленные ячейки; если ячейки нет — отметка ещё не проставлена")
        List<AttendanceCellResponse> attendances) {}
