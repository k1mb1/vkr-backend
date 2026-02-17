package com.github.k1mb1.vkr_backend.domain.attendances.requests;

import com.github.k1mb1.vkr_backend.domain.attendances.PresenceType;

public record UpdateAttendanceRequest(
        String note,
        PresenceType presence
) {
}