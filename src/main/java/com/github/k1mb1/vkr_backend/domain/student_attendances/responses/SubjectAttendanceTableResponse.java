package com.github.k1mb1.vkr_backend.domain.student_attendances.responses;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Flat attendance table for a subject.
 *
 * <p>Front-end builds the grid by matching {@code attendances} via
 * {@code (lessonId, studentId)} against the {@code lessons} (columns)
 * and {@code students} (rows) arrays.
 */
public record SubjectAttendanceTableResponse(
    List<SubjectLessonTableEntryResponse> lessons,
    List<StudentEntryResponse> students,
    List<AttendanceCellResponse> attendances
) {
    public record SubjectLessonTableEntryResponse(
        UUID lessonId,
        String lessonName,
        OffsetDateTime dateTime
    ) {}
}
