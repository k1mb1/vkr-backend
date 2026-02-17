package com.github.k1mb1.vkr_backend.domain.grades.responses;

import java.util.UUID;

public record GradeResponse(
        UUID id,
        String comment,
        Integer value,
        UUID lessonId,
        UUID studentId
) {
}
