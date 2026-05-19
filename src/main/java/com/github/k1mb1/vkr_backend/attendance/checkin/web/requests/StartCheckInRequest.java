package com.github.k1mb1.vkr_backend.attendance.checkin.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@Schema(description = "Запуск опроса по посещаемости")
public record StartCheckInRequest(
    @NotNull
    @Schema(
        description = "ID занятия. Аудитория опроса совпадает с аудиторией занятия " +
            "(все группы и подгруппы, заданные в lesson scopes; либо все группы предмета при allGroups=true).",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID lessonId,

    @NotNull
    @Min(1)
    @Schema(
        description = "Длительность основного окна (секунды). Студенты, отметившиеся здесь, получат статус PRESENT",
        requiredMode = Schema.RequiredMode.REQUIRED, example = "600"
    )
    Integer onTimeSeconds,

    @NotNull
    @PositiveOrZero
    @Schema(
        description = "Дополнительное окно для опоздавших (секунды). Отметившиеся здесь получат статус LATE",
        requiredMode = Schema.RequiredMode.REQUIRED, example = "600"
    )
    Integer lateSeconds
) {}
