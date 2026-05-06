package com.github.k1mb1.vkr_backend.student_groups.web.responses;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SubgroupResponse(
        UUID id,
        String name
) {}
