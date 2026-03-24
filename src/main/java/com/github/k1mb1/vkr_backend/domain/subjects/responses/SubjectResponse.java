package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import java.time.Instant;
import java.util.UUID;

public record SubjectResponse(
    UUID id,
    String name,
    String description,
    boolean archived,
    Instant archivedAt,
    Instant createdAt,
    Instant updatedAt
) {}
