package com.github.k1mb1.vkr_backend.domain.student_grades.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCreateGradeRequest(
        @NotEmpty @Valid List<BulkGradeItem> items
) {}
