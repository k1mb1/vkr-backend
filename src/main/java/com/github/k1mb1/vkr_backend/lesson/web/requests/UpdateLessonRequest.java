package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(
    description = """
        Запрос на частичное обновление занятия (PATCH).
        Семантика: поле = null или отсутствует — значение не изменяется.
        Если передан allGroups=false, то scopes должен быть непустым.
        """
)
public record UpdateLessonRequest(
    @Schema(description = "ID предмета (null — не менять)")
    UUID subjectId,

    @Schema(description = "Тип занятия (null — не менять)")
    LessonType type,

    @Schema(description = "Дата занятия (null — не менять)")
    LocalDate startedAt,

    @Schema(description = "Тема занятия (null — не менять)")
    String topic,

    @Schema(description = "true = все группы предмета (null — не менять)")
    Boolean allGroups,

    @Schema(
        description = "Список scopes (null — не менять). " + "Если задан вместе с allGroups=false, должен быть непустым."
    )
    @Valid
    List<LessonScopeRequest> scopes
) {}
