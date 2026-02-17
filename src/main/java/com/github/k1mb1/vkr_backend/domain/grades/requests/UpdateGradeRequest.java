package com.github.k1mb1.vkr_backend.domain.grades.requests;

public record UpdateGradeRequest(
        String comment,
        Integer value
) {
}