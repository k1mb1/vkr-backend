package com.github.k1mb1.vkr_backend.domain.teachers.responses;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link TeacherEntity}
 */
public record TeacherResponse(
    UUID id,
    String username,
    String email,
    Instant createdAt,
    Instant updatedAt
) {}
