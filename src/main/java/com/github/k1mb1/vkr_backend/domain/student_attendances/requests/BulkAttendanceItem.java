package com.github.k1mb1.vkr_backend.domain.student_attendances.requests;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BulkAttendanceItem(
        @NotNull UUID studentId,
        @NotNull PresenceType presence,
        String note
) {}
