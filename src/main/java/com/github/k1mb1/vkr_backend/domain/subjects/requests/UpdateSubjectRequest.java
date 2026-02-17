package com.github.k1mb1.vkr_backend.domain.subjects.requests;

public record UpdateSubjectRequest(
        String name,
        String description
) {}