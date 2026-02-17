package com.github.k1mb1.vkr_backend.domain.attendances.responses;

import com.github.k1mb1.vkr_backend.domain.attendances.PresenceType;

import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        String note,
        PresenceType presence,
        UUID lessonId,
        UUID studentId
) {}