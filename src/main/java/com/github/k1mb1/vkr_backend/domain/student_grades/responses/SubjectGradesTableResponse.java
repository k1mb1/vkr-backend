package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Flat grade table for a subject.
 *
 * <p>Front-end builds the grid by matching {@code grades} via
 * {@code (taskId, studentId)} against the {@code lessons} (column groups)
 * and {@code students} (rows) arrays.
 */
public record SubjectGradesTableResponse(
    List<SubjectLessonTableEntryResponse> lessons,
    List<StudentEntryResponse> students,
    List<GradeCellResponse> grades
) {
    public record SubjectLessonTableEntryResponse(
        UUID lessonId,
        String lessonName,
        OffsetDateTime dateTime
    ) {}
}
