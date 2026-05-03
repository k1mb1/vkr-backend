package com.github.k1mb1.vkr_backend.education.attendance.api;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentEntryResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public record AttendanceTableResponse(
    List<LessonEntry> lessons,
    List<StudentEntryResponse> students,
    List<AttendanceCellResponse> attendances
) {
    public record LessonEntry(UUID lessonId, String lessonName, OffsetDateTime dateTime, LessonType type, UUID groupId) {}
}
