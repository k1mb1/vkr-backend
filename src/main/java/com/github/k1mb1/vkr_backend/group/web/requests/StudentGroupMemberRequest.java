package com.github.k1mb1.vkr_backend.group.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record StudentGroupMemberRequest(
    @NotBlank String username,
    @PositiveOrZero Short subgroupIndex
) {}
