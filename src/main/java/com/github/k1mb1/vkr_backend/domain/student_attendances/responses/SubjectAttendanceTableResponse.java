package com.github.k1mb1.vkr_backend.domain.student_attendances.responses;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SubjectAttendanceTableResponse(
        List<SubjectLessonTableEntryResponse> lessons,
        List<StudentAttendanceTableResponse> students
) {
    public record SubjectLessonTableEntryResponse(
            UUID lessonId,
            String lessonName,
            OffsetDateTime dateTime
    ) {}

    public record StudentAttendanceTableResponse(
            UUID studentId,
            String username,
            List<AttendanceEntryResponse> attendances
    ) {}
}
