package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import java.util.List;
import java.util.UUID;

public record StudentGradesResponse(
    UUID studentId,
    String studentUsername,
    List<GradeEntryResponse> grades
) {}
