package com.github.k1mb1.vkr_backend.domain.attendances.requests;

import com.github.k1mb1.vkr_backend.domain.attendances.PresenceType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAttendanceRequest(
        String note,
        @NotNull PresenceType presence,
        @NotNull UUID lessonId,
        @NotNull UUID studentId
) {
}