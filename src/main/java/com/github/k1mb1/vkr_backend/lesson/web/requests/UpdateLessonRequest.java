package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@Schema(
    description = """
        Запрос на частичное обновление шаблона занятия (PATCH).
        Семантика: поле = null или отсутствует — значение не изменяется.
        Если передан scopes, он должен быть непустым.
        """
)
public record UpdateLessonRequest(
    @Schema(description = "ID предмета (null — не менять)")
    UUID subjectId,

    @Schema(description = "Тип занятия (null — не менять)")
    LessonType type,

    @Schema(description = "Тема занятия (null — не менять)")
    String topic,

    @Schema(description = "Порядковый номер в рамках (предмет, тип). null — не менять")
    Integer orderIndex,

    @Schema(description = "Список scopes (null — не менять, иначе полностью замещает существующие).")
    @Valid
    List<LessonScopeRequest> scopes
) {}
