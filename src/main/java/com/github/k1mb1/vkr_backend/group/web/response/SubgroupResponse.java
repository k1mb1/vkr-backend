package com.github.k1mb1.vkr_backend.group.web.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubgroupResponse(
    UUID id,
    Integer index,
    Instant createdAt,
    Instant updatedAt
) {}
