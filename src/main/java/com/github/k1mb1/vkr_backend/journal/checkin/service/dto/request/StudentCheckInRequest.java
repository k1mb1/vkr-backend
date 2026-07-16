package com.github.k1mb1.vkr_backend.journal.checkin.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Отметка студента в check-in сессии")
public record StudentCheckInRequest(
        @NotNull @Schema(
                description = "ID студента (берётся из результата поиска по фамилии)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID studentId,

        @NotBlank @Size(max = 32) @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Код аудитории содержит только латинские буквы и цифры") @Schema(
                description = "Код аудитории, который преподаватель показал в аудитории. "
                        + "Без верного кода отметка отклоняется — это не даёт отметить кого-либо, "
                        + "просто зная его ID.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String code) {}
