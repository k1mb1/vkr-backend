package com.github.k1mb1.vkr_backend.attendance.checkin.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
    description = "Поиск себя по фамилии на публичной странице check-in. Требует кода аудитории: " +
        "список доступен только тем, кто знает код."
)
public record PublicStudentSearchRequest(
    @NotBlank
    @Schema(
        description = "Код аудитории, показанный преподавателем",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String code,

    @Schema(
        description = "Фамилия или часть ФИО для поиска. Короче 2 символов — вернётся пустой список."
    )
    String query
) {}
