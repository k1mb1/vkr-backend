package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.responses.TaskResponse;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import java.util.List;

/**
 * Flat grade table for a single lesson.
 *
 * <p>Front-end matches {@code grades} via {@code (taskId, studentId)} against
 * the {@code tasks} array (columns) and {@code students} array (rows).
 */
public record LessonGradeTableResponse(
    List<TaskResponse> tasks,
    List<StudentEntryResponse> students,
    List<GradeCellResponse> grades
) {}
