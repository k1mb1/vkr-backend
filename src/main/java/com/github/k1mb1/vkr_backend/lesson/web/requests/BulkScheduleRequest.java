package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(
    description = "Запрос на массовое создание занятий по недельному шаблону"
)
public record BulkScheduleRequest(
    @Schema(
        description = "ID предмета",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID subjectId,

    @Schema(
        description = "ID группы",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID groupId,

    @Schema(
        description = "Список шаблонов расписания",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty
    List<@Valid Entry> schedules
) {
    @Schema(description = "Элемент шаблона расписания")
    public record Entry(
        @Schema(
            description = "Тип занятия",
            requiredMode = Schema.RequiredMode.REQUIRED
        ) @NotNull LessonType type,
        @Schema(
            description = "Дата начала (первая неделя)",
            requiredMode = Schema.RequiredMode.REQUIRED
        ) @NotNull LocalDate startDate,
        @Schema(
            description = "Общее количество занятий",
            requiredMode = Schema.RequiredMode.REQUIRED
        ) @NotNull @Min(1) Integer totalCount,
        @Schema(
            description = "Дни недели по неделям. Внешний список — недели, внутренний — дни недели внутри недели",
            requiredMode = Schema.RequiredMode.REQUIRED
        ) @NotEmpty List<@NotEmpty List<@NotNull DayOfWeek>> daysOfWeek
    ) {}
}
