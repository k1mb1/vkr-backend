package com.github.k1mb1.vkr_backend.domain.student_attendances.responses;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;
import java.util.UUID;

public record StudentAttendanceResponse(
    UUID id,
    String note,
    PresenceType presence,
    UUID lessonId,
    UUID studentId
) {}
