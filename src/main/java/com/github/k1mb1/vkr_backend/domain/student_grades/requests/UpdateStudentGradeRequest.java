package com.github.k1mb1.vkr_backend.domain.student_grades.requests;

public record UpdateStudentGradeRequest(
        String comment,
        Integer value
) {
}