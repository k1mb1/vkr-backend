package com.github.k1mb1.vkr_backend.group.web.response;

import java.time.Instant;
import java.util.UUID;

public record GroupPageResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {}

