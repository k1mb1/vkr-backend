package com.github.k1mb1.vkr_backend.group;

import java.time.Instant;
import java.util.UUID;

public record SubgroupResponse(
    UUID id,
    Short index,
    Instant createdAt,
    Instant updatedAt
) {}
