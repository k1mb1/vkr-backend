package com.github.k1mb1.vkr_backend.domain.grades.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGradeRequest(
        String comment,
        @NotNull Integer value,
        @NotNull UUID lessonId,
        @NotNull UUID studentId
) {
}