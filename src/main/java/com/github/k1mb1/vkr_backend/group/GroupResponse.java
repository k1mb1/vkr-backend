package com.github.k1mb1.vkr_backend.group;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
    UUID id,
    String name,
    Instant createdAt,
    Instant updatedAt
) {}
