package com.github.k1mb1.vkr_backend.domain.subjects.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateSubjectRequest(
        @NotBlank String name,
        String description
) {
}
