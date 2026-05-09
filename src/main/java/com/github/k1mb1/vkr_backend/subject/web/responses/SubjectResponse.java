package com.github.k1mb1.vkr_backend.subject.web.responses;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubjectResponse(
    UUID id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
