package com.github.k1mb1.vkr_backend.subject.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Право преподавателя на предмет")
public record TeacherSubjectPermissionResponse(
    @Schema(description = "ID права")
    UUID id,

    @Schema(description = "ID преподавателя")
    UUID teacherId,

    @Schema(description = "Имя преподавателя")
    String teacherName,

    @Schema(description = "ID предмета")
    UUID subjectId,

    @Schema(
        description = "true = преподаватель имеет доступ ко всем группам предмета"
    )
    boolean allPermissions,

    @Schema(
        description = "Список scope'ов (группа с полным списком подгрупп + опц. разрешённая подгруппа + опц. тип занятия). " + "При allPermissions=true scope'ы синтезируются из групп предмета (по одному на группу, без ограничений по подгруппе и типу)."
    )
    List<PermissionScopeResponse> scopes,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
