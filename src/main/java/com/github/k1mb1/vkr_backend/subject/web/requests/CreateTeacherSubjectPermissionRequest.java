package com.github.k1mb1.vkr_backend.subject.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Запрос на создание права преподавателя на предмет")
public record CreateTeacherSubjectPermissionRequest(
    @Schema(description = "ID преподавателя", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    UUID teacherId,

    @Schema(description = "ID предмета", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    UUID subjectId,

    @Schema(description = "ID группы", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    UUID groupId,

    @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
    UUID allowedSubgroupId,

    @Schema(description = "Разрешённый тип занятия (null = все типы)")
    LessonType allowedLessonType
) {}
