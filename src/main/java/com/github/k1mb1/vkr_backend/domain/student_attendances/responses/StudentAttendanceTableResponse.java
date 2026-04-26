package com.github.k1mb1.vkr_backend.domain.student_attendances.responses;

import java.util.List;
import java.util.UUID;

public record StudentAttendanceTableResponse(
    UUID studentId,
    String username,
    List<AttendanceEntryResponse> attendances
) {}
