package com.github.k1mb1.vkr_backend.domain.student_grades.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BulkGradeItem(
        @NotNull UUID studentId,
        @NotNull Integer value,
        String comment
) {}
