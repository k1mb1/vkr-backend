package com.github.k1mb1.vkr_backend.domain.student_attendances.requests;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateStudentAttendanceRequest(
        String note,
        @NotNull PresenceType presence,
        @NotNull UUID lessonId,
        @NotNull UUID studentId
) {
}