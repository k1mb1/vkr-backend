package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import java.time.Instant;
import java.util.UUID;

public record StudentGroupResponse(
    UUID id,
    String name,
    Instant createdAt,
    Instant updatedAt
) {}
