package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StudentGroupDetailResponse(
        UUID id,
        String name,
        List<UUID> studentIds,
        Instant createdAt,
        Instant updatedAt
) {
}