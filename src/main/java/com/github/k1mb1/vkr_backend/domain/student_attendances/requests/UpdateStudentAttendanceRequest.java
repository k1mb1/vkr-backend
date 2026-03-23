package com.github.k1mb1.vkr_backend.domain.student_attendances.requests;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;

public record UpdateStudentAttendanceRequest(
    String note,
    PresenceType presence
) {}
