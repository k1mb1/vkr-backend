package com.github.k1mb1.vkr_backend.teacher.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = """
        Запрос на частичное обновление права преподавателя на предмет (PATCH).
        Поле = null → значение не изменяется.
        scopes (если указан) полностью заменяет текущий набор scope'ов.
        """)
public record UpdateTeacherSubjectPermissionRequest(
        @Schema(description = "ID преподавателя (null — не менять)")
        UUID teacherId,

        @Schema(description = "true/false — изменить флаг allPermissions (null — не менять)")
        Boolean allPermissions,

        @Schema(
                description = "Новый список scopes (null — не менять). Если задан — должен быть непустым.",
                types = {"array", "null"})
        @Valid @Size(max = 200) List<PermissionScopeRequest> scopes) {

    @Schema(hidden = true)
    @AssertTrue(message = "Если scopes указан, он должен быть непустым") public boolean isScopesNonEmptyWhenProvided() {
        return scopes == null || !scopes.isEmpty();
    }
}
