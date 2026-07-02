package com.github.k1mb1.vkr_backend.subject.web.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record GradingHighlightPolicyRequest(
    @NotNull Boolean enabled,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String assignmentColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String fullColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String partialLowColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String partialHighColor
) {}
