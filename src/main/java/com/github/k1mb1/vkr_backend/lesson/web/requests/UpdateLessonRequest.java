package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(
    description = """
    Запрос на частичное обновление занятия (PATCH).
    Семантика: поле = null или отсутствует — значение не изменяется.
    Очистка существующих значений (например, "отвязать подгруппу") через этот эндпоинт не поддерживается.
    """
)
public record UpdateLessonRequest(
    @Schema(description = "ID предмета (null — не менять)") UUID subjectId,

    @Schema(description = "ID группы (null — не менять)") UUID groupId,

    @Schema(description = "Тип занятия (null — не менять)") LessonType type,

    @Schema(description = "Дата занятия (null — не менять)")
    LocalDate startedAt,

    @Schema(description = "Тема занятия (null — не менять)") String topic,

    @Schema(
        description = "ID подгруппы (null — не менять; очистить через этот эндпоинт нельзя)"
    )
    UUID subgroupId
) {}
