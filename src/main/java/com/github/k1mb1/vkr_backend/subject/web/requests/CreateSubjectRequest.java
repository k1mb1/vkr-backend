package com.github.k1mb1.vkr_backend.subject.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSubjectRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID groupId,
        @NotNull UUID teacherId
) {}