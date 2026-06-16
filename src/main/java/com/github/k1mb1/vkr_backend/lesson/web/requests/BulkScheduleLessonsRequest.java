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
import java.util.Objects;
import java.util.UUID;

@Schema(
    description = """
    Создание одного занятия с серией проведений по недельному шаблону.
    `days` — повторяющийся шаблон: внешний список = недели, внутренний = дни недели внутри недели.
    Генерация идёт по неделям, шаблон зацикливается, пока не наберётся `count` дат проведений.
    Создаётся одно занятие-шаблон выбранного типа; на каждую вычисленную дату создаётся по одному
    scope'у (проведению) для каждой указанной аудитории — итого `count` × `audiences.size()` scope'ов.
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

    @Schema(
        description = "Аудитории для созданных пар: на каждую дату создаётся scope для каждой " +
            "аудитории (группа + опциональная подгруппа). null или пустой список = все группы."
    )
    @Valid
    List<@NotNull LessonScopeAudienceRequest> audiences
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

    @Schema(hidden = true)
    @AssertTrue(
        message = "Audiences must not overlap (same group or nested subgroup) within the request"
    )
    public boolean hasNoOverlappingAudiences() {
        if (audiences == null) {
            return true;
        }
        for (int i = 0; i < audiences.size(); i++) {
            for (int j = i + 1; j < audiences.size(); j++) {
                if (audiencesOverlap(audiences.get(i), audiences.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean audiencesOverlap(
        LessonScopeAudienceRequest a,
        LessonScopeAudienceRequest b
    ) {
        if (a == null || b == null) {
            return false; // отловит @NotNull на элементах
        }
        if (!Objects.equals(a.groupId(), b.groupId())) {
            return false;
        }
        // Та же группа: вся группа пересекается с чем угодно; иначе — только одинаковые подгруппы.
        if (a.allowedSubgroupId() == null || b.allowedSubgroupId() == null) {
            return true;
        }
        return Objects.equals(a.allowedSubgroupId(), b.allowedSubgroupId());
    }
}
