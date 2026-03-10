package com.github.k1mb1.vkr_backend.domain.teachers.requests;

import jakarta.validation.constraints.NotBlank;

public record FindOrCreateTeacherRequest(
        @NotBlank String username,
        @NotBlank String email
) {}
