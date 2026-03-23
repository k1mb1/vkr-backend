package com.github.k1mb1.vkr_backend.domain.students.responses;

import java.time.Instant;
import java.util.UUID;

public record StudentResponse(
    UUID id,
    String username,
    UUID groupId,
    Instant createdAt,
    Instant updatedAt
) {}
