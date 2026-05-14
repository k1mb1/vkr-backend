package com.github.k1mb1.vkr_backend.subject.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Запрос на обновление права преподавателя на предмет")
public record UpdateTeacherSubjectPermissionRequest(
    @Schema(description = "ID преподавателя")
    UUID teacherId,

    @Schema(description = "ID группы")
    UUID groupId,

    @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
    UUID allowedSubgroupId,

    @Schema(description = "Разрешённый тип занятия (null = все типы)")
    LessonType allowedLessonType
) {}
