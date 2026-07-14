package com.github.k1mb1.vkr_backend.lesson.service.dto.request;

import com.github.k1mb1.vkr_backend.subject.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(description = """
    Создание серии из `count` занятий по недельным шаблонам.
    `count` — общий для всех элементов: для каждого элемента `items` генерируется ровно `count` дат,
    и создаётся `count` занятий-шаблонов выбранного типа.
    Каждый элемент `items` задаёт одну аудиторию со своим собственным расписанием
    (`firstLessonDate` + `days`).
    `days` — повторяющийся шаблон: внешний список = недели, внутренний = дни недели внутри недели.
    Генерация идёт по неделям, шаблон зацикливается, пока не наберётся `count` дат проведений.
    Занятие k проводится на k-ю дату каждого элемента: на него создаётся по одному scope'у
    (проведению) для аудитории каждого элемента — итого `count` занятий, каждое с `items.size()`
    scope'ами.
    Пример: items=[{audience, firstLessonDate=понедельник, days=[["MONDAY","THURSDAY"], []]}], count=5
    → 5 занятий на пн/чт 1-й недели, пн/чт 3-й недели, пн 5-й недели (раз в 2 недели).
    """)
public record BulkScheduleLessonsRequest(
        @Schema(description = "ID предмета", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID subjectId,

        @Schema(description = "Тип создаваемого занятия", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull LessonType lessonType,

        @Schema(
                description = "Сколько проведений (дат) создать для каждого элемента items",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive @Max(500) int count,

        @Schema(
                description =
                        "Элементы расписания: каждая аудитория со своим собственным " + "стартом и недельным шаблоном.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid @Size(max = 100) List<@NotNull Item> items) {
    @Schema(
            name = "ScheduleLessonItem",
            description = "Один элемент расписания: аудитория + её собственный старт и недельный шаблон.")
    public record Item(
            @Schema(
                    description =
                            "Аудитория для созданных пар (группа + опциональная подгруппа). " + "null = все группы.",
                    types = {"object", "null"})
            @Valid LessonScopeAudienceRequest audience,

            @Schema(description = "Дата первой пары (первого проведения)", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull LocalDate firstLessonDate,

            @Schema(
                    description = "Недельный шаблон. Внешний список — недели, внутренний — дни недели этой недели.",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotEmpty List<@NotNull List<@NotNull DayOfWeek>> days) {
        @Schema(hidden = true)
        @AssertTrue(message = "At least one week in the pattern must contain a day") public boolean hasAnyDay() {
            return days != null && days.stream().anyMatch(week -> week != null && !week.isEmpty());
        }

        @Schema(hidden = true)
        @AssertTrue(message = "firstLessonDate must fall on the earliest weekday of the first non-empty week") public boolean isFirstLessonDateConsistent() {
            if (days == null || firstLessonDate == null) {
                return true; // отловят @NotNull / @NotEmpty
            }
            return days.stream()
                    .filter(week -> week != null && !week.isEmpty())
                    .findFirst()
                    .map(week -> week.stream().min(Comparator.naturalOrder()).orElseThrow())
                    .map(earliest -> firstLessonDate.getDayOfWeek() == earliest)
                    .orElse(true);
        }
    }

    @Schema(hidden = true)
    @AssertTrue(message = "Audiences must not overlap (same group or nested subgroup) within the request")
    public boolean hasNoOverlappingAudiences() {
        if (items == null) {
            return true;
        }
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                if (itemsOverlap(items.get(i), items.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean itemsOverlap(Item a, Item b) {
        if (a == null || b == null) {
            return false; // отловит @NotNull на элементах
        }
        return audiencesOverlap(a.audience(), b.audience());
    }

    private static boolean audiencesOverlap(LessonScopeAudienceRequest a, LessonScopeAudienceRequest b) {
        // null-аудитория = все группы: пересекается с любой другой аудиторией.
        if (a == null || b == null) {
            return true;
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
