package com.github.k1mb1.vkr_backend.grading.web.responses;

import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.GradingHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(
    description = "Таблица оценок: студенты × (задания + extra-колонка) с существующими ячейками"
)
public record GradingTableResponse(
    @Schema(
        description = "Политика понижения за просрочку и бонуса за раннюю сдачу. Если включена — " +
            "фронт применяет её к каждой ячейке по её lessonsOffset."
    )
    PenaltyPolicyResponse penaltyPolicy,

    @Schema(
        description = "Связка посещаемости с баллом. Если enabled=true — фронт считает вклад " +
            "посещаемости по сводке attendance ниже."
    )
    AttendancePolicyResponse attendancePolicy,

    @Schema(
        description = "Цветовая подсветка таблицы оценок. Если enabled=true — фронт раскрашивает " +
            "колонки заданий и ячейки по этим цветам (HEX вида #00C16A)."
    )
    GradingHighlightPolicyResponse highlightPolicy,

    @Schema(
        description = "Промежуточная аттестация (итоги). Если enabled=true — фронт по итоговому " +
            "баллу/задачам выбирает первую подходящую банду и считает «сколько до следующей»."
    )
    FinalAssessmentPolicyResponse finalAssessmentPolicy,

    @Schema(
        description = "Сводка посещаемости по студентам (по занятиям этой таблицы) для расчёта вклада"
    )
    List<StudentAttendanceResponse> attendance,

    @Schema(
        description = "Аудитория таблицы — группы/подгруппы из scope'ов разрешения. " +
            "Пустой список при allPermissions=true: клиент должен взять группы из subject.groups."
    )
    List<GradingAudienceScope> audience,

    @Schema(description = "Строки таблицы — студенты")
    List<GradingTableStudent> students,

    @Schema(
        description = "Занятия (нужны для построения подзаголовков колонок и extra-оценок по уроку)"
    )
    List<GradingTableLesson> lessons,

    @Schema(description = "Задания (колонки таблицы внутри каждого урока)")
    List<AssignmentResponse> assignments,

    @Schema(
        description = "Проставленные ячейки оценок (включая оценки вне заданий — у них assignmentId=null)"
    )
    List<GradeCellResponse> grades
) {}
