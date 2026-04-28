package com.github.k1mb1.vkr_backend.domain.student_attendances.responses;

import com.github.k1mb1.vkr_backend.domain.student_attendances.PresenceType;
import java.util.UUID;

/**
 * Flat attendance cell for a subject grade/attendance table.
 *
 * @param attendanceId Attendance record UUID.
 * @param lessonId     Lesson UUID (column key).
 * @param studentId    Student UUID (row key).
 * @param presence     Presence status.
 * @param note         Optional teacher note.
 */
public record AttendanceCellResponse(
    UUID attendanceId,
    UUID lessonId,
    UUID studentId,
    PresenceType presence,
    String note
) {}
