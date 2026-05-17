package com.github.k1mb1.vkr_backend.subject.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    description = """
    Запрос на частичное обновление права преподавателя на предмет (PATCH).
    Семантика: поле = null или отсутствует — значение не изменяется.
    Очистка существующих значений (например, отвязать подгруппу или сбросить тип занятия) через этот эндпоинт не поддерживается.
    """
)
public record UpdateTeacherSubjectPermissionRequest(
    @Schema(description = "ID преподавателя (null — не менять)") UUID teacherId,

    @Schema(description = "ID группы (null — не менять)") UUID groupId,

    @Schema(
        description = "ID разрешённой подгруппы (null — не менять; очистить через этот эндпоинт нельзя)"
    )
    UUID allowedSubgroupId,

    @Schema(
        description = "Разрешённый тип занятия (null — не менять; очистить через этот эндпоинт нельзя)"
    )
    LessonType allowedLessonType
) {}
