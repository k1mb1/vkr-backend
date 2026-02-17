package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import java.util.UUID;

public record StudentGradeResponse(
        UUID id,
        String comment,
        Integer value,
        UUID lessonId,
        UUID studentId
) {
}
