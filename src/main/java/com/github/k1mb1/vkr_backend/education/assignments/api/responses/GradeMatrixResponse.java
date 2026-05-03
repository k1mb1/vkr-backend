package com.github.k1mb1.vkr_backend.education.assignments.api.responses;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.StudentEntryResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public record GradeMatrixResponse(
    List<LessonEntry> lessons,
    List<StudentEntryResponse> students,
    List<GradeCellResponse> grades
) {
    public record LessonEntry(UUID lessonId, String lessonName, OffsetDateTime dateTime, LessonType type, UUID groupId, List<TaskResponse> tasks) {}
}
