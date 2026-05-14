package com.github.k1mb1.vkr_backend.subject.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    description = "Плоская строка таблицы преподавания (группа × учитель × тип × подгруппа)"
)
public record SubjectTeachingRowResponse(
    @Schema(description = "ID права") UUID permissionId,

    @Schema(description = "ID группы") UUID groupId,

    @Schema(description = "Название группы") String groupName,

    @Schema(description = "ID преподавателя") UUID teacherId,

    @Schema(description = "Имя преподавателя") String teacherName,

    @Schema(description = "Разрешённый тип занятия (null = все)")
    LessonType allowedLessonType,

    @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
    UUID allowedSubgroupId,

    @Schema(description = "Индекс разрешённой подгруппы")
    Integer allowedSubgroupIndex
) {}
