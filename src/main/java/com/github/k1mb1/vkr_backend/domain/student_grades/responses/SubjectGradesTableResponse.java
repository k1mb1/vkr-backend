package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SubjectGradesTableResponse(
    List<SubjectLessonTableEntryResponse> lessons,
    List<StudentTaskGradesResponse> students
) {
    public record SubjectLessonTableEntryResponse(
        UUID lessonId,
        String lessonName,
        OffsetDateTime dateTime
    ) {}
}
