package com.github.k1mb1.vkr_backend.domain.student_groups.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Student entry for group creation.
 *
 * @param username      Student display name.
 * @param subgroupIndex 0-based subgroup index. {@code null} means the student
 *                      is placed directly in the main group (no subgroup).
 */
public record StudentGroupMemberRequest(
    @NotBlank String username,
    @Min(0) Integer subgroupIndex
) {}
