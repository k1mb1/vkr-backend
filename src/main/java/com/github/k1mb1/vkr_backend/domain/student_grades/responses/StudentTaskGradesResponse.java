package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import java.util.List;
import java.util.UUID;

/**
 * All task grades for one student within a lesson.
 *
 * @param studentId Student UUID.
 * @param username  Student display name.
 * @param grades    Grades per task, ordered by task position.
 */
public record StudentTaskGradesResponse(
    UUID studentId,
    String username,
    List<TaskGradeResponse> grades
) {}
