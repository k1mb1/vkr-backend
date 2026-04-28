package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SubgroupResponse(
    UUID id,
    String name
) {}
