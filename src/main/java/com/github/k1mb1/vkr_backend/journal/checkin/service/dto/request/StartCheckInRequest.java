package com.github.k1mb1.vkr_backend.journal.checkin.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Запуск опроса по посещаемости")
public record StartCheckInRequest(
        @NotNull @Schema(
                description =
                        "ID конкретного проведения занятия (lesson_scope). "
                                + "Аудитория опроса совпадает с группой/подгруппой этого scope (или со всеми группами предмета при allGroups=true).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID lessonScopeId,

        @Min(1) @Max(86_400) @Schema(
                description =
                        "Длительность основного окна (секунды). Студенты, отметившиеся здесь, получат статус PRESENT. "
                                + "Обязательно, если у предмета НЕ задана политика check-in; при заданной политике игнорируется.",
                example = "600")
        Integer onTimeSeconds,

        @PositiveOrZero @Max(86_400) @Schema(
                description =
                        "Дополнительное окно для опоздавших (секунды). Отметившиеся здесь получат статус LATE. "
                                + "Обязательно, если у предмета НЕ задана политика check-in; при заданной политике игнорируется.",
                example = "600")
        Integer lateSeconds) {}
