package com.github.k1mb1.vkr_backend.subject.web.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AttendanceHighlightPolicyRequest(
    @NotNull Boolean enabled,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String presentColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String lateColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String absentColor,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String excusedColor
) {}
