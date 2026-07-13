package com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request;

import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Schema(
        description = "Подтверждение результатов check-in. Записи без отметки трактуются как ABSENT. "
                + "В overrides можно изменить статус или добавить комментарий перед переносом в основную посещаемость.")
public record ConfirmCheckInRequest(
        @Valid @Schema(description = "Ручные изменения статуса / комментариев для отдельных студентов")
        List<StudentOverride> overrides) {
    @Schema(name = "ConfirmCheckInOverride", description = "Ручное переопределение статуса студента")
    public record StudentOverride(
            @NotNull @Schema(description = "ID студента", requiredMode = Schema.RequiredMode.REQUIRED)
            UUID studentId,

            @NotNull @Schema(description = "Финальный статус посещаемости", requiredMode = Schema.RequiredMode.REQUIRED)
            AttendanceStatus status,

            @Schema(description = "Комментарий") @Nullable String comment) {}
}
