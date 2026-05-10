package com.github.k1mb1.vkr_backend.group.web.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record StudentGroupMemberRequest(
    @NotBlank String username,
    @PositiveOrZero Integer subgroupIndex
) {}
