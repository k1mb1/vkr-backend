package com.github.k1mb1.vkr_backend.subject.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Право преподавателя на предмет")
public record TeacherSubjectPermissionResponse(
    @Schema(description = "ID права")
    UUID id,

    @Schema(description = "ID преподавателя")
    UUID teacherId,

    @Schema(description = "ID предмета")
    UUID subjectId,

    @Schema(description = "ID группы")
    UUID groupId,

    @Schema(description = "ID разрешённой подгруппы")
    UUID allowedSubgroupId,

    @Schema(description = "Разрешённый тип занятия")
    LessonType allowedLessonType,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
