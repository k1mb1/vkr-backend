package com.github.k1mb1.vkr_backend.domain.teachers.responses;

import java.time.Instant;
import java.util.UUID;

public record TeacherResponse(
        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}