package com.github.k1mb1.vkr_backend.student.internal.web.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentResponse(
    UUID id,
    String username,
    UUID groupId,
    UUID subgroupId,
    Instant createdAt,
    Instant updatedAt
) {}
