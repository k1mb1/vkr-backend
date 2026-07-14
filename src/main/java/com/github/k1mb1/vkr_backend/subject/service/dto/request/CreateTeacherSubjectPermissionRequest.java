package com.github.k1mb1.vkr_backend.subject.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на создание права преподавателя на предмет")
public record CreateTeacherSubjectPermissionRequest(
        @Schema(description = "ID преподавателя", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID teacherId,

        @Schema(description = "ID предмета", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID subjectId,

        @Schema(
                description = "true = полный доступ ко всем группам предмета; false = только перечисленные в scopes",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean allPermissions,

        @Schema(
                description = "Список scopes (group + опц. подгруппа + опц. тип занятия). "
                        + "Обязателен и должен быть непустым при allPermissions=false. "
                        + "При allPermissions=true игнорируется.")
        @Valid @Size(max = 200) List<PermissionScopeRequest> scopes) {

    @Schema(hidden = true)
    @AssertTrue(message = "Список scopes должен быть непустым, когда allPermissions = false") public boolean isScopesPresentWhenNotAllPermissions() {
        if (Boolean.TRUE.equals(allPermissions)) {
            return true;
        }
        return scopes != null && !scopes.isEmpty();
    }
}
