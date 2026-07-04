package com.github.k1mb1.vkr_backend.attendance.checkin.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Отметка студента в check-in сессии")
public record StudentCheckInRequest(
        @NotNull @Schema(
                description = "ID студента (берётся из результата поиска по фамилии)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID studentId,

        @NotBlank @Schema(
                description = "Код аудитории, который преподаватель показал в аудитории. "
                        + "Без верного кода отметка отклоняется — это не даёт отметить кого-либо, "
                        + "просто зная его ID.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String code) {}
