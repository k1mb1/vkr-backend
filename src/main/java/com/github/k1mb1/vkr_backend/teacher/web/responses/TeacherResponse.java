package com.github.k1mb1.vkr_backend.teacher.web.responses;

import java.time.Instant;
import java.util.UUID;

public record TeacherResponse(
    UUID id,
    String username,
    String email,
    Instant createdAt,
    Instant updatedAt
) {}
