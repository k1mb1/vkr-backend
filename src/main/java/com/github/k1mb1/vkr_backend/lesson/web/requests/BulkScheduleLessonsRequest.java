package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Schema(
    description = """
    Создание одного занятия с серией проведений по недельному шаблону.
    `days` — повторяющийся шаблон: внешний список = недели, внутренний = дни недели внутри недели.
    Генерация идёт по неделям, шаблон зацикливается, пока не наберётся `count` дат проведений.
    Создаётся одно занятие-шаблон выбранного типа и `count` scope'ов (проведений) для указанной
    аудитории — по одному на каждую вычисленную дату.
    Пример: firstLessonDate=понедельник, days=[["MONDAY","THURSDAY"], []], count=5
    → проведения на пн/чт 1-й недели, пн/чт 3-й недели, пн 5-й недели (раз в 2 недели).
    """
)
public record BulkScheduleLessonsRequest(
    @Schema(
        description = "ID предмета",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID subjectId,

    @Schema(
        description = "Тип создаваемого занятия",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    LessonType lessonType,

    @Schema(
        description = "Дата первой пары (первого проведения)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    LocalDate firstLessonDate,

    @Schema(
        description = "Сколько проведений (дат) создать",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Positive
    int count,

    @Schema(
        description = "Недельный шаблон. Внешний список — недели, внутренний — дни недели этой недели.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    List<@NotNull List<@NotNull DayOfWeek>> days,

    @Schema(description = "Аудитория для всех созданных пар. null = все группы")
    @Valid
    LessonScopeAudienceRequest audience
) {
    @Schema(hidden = true)
    @AssertTrue(message = "At least one week in the pattern must contain a day")
    public boolean hasAnyDay() {
        return (
            days != null &&
            days.stream().anyMatch(week -> week != null && !week.isEmpty())
        );
    }

    @Schema(hidden = true)
    @AssertTrue(
        message = "firstLessonDate must fall on the earliest weekday of the first non-empty week"
    )
    public boolean isFirstLessonDateConsistent() {
        if (days == null || firstLessonDate == null) {
            return true; // отловят @NotNull / @NotEmpty
        }
        return days
            .stream()
            .filter(week -> week != null && !week.isEmpty())
            .findFirst()
            .map(week ->
                week.stream().min(Comparator.naturalOrder()).orElseThrow()
            )
            .map(earliest -> firstLessonDate.getDayOfWeek() == earliest)
            .orElse(true);
    }
}
