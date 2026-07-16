package com.github.k1mb1.vkr_backend.teacher.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Право преподавателя на предмет")
public record TeacherSubjectPermissionResponse(
        @Schema(description = "ID права") UUID id,

        @Schema(description = "ID преподавателя") UUID teacherId,

        @Schema(description = "Имя преподавателя") String teacherName,

        @Schema(description = "ID предмета") UUID subjectId,

        @Schema(
                description =
                        "true = у преподавателя полный доступ к предмету (любая группа/подгруппа/тип занятия). "
                                + "В этом случае scopes возвращается пустым — клиент должен брать аудиторию из subject.groups напрямую.")
        boolean allPermissions,

        @Schema(
                description =
                        "Список scope'ов (группа с полным списком подгрупп + опц. разрешённая подгруппа + опц. тип занятия). "
                                + "Пустой список при allPermissions=true.")
        List<PermissionScopeResponse> scopes,

        @Schema(description = "Дата создания") Instant createdAt,

        @Schema(description = "Дата последнего обновления") Instant updatedAt) {}
