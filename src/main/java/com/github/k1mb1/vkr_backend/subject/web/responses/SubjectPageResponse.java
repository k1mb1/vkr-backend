package com.github.k1mb1.vkr_backend.subject.web.responses;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record SubjectPageResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}