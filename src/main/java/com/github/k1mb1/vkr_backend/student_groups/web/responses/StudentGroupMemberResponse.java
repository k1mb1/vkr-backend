package com.github.k1mb1.vkr_backend.student_groups.web.responses;

import java.util.UUID;
import lombok.Builder;

/**
 * Student entry inside a group response.
 *
 * @param id Student UUID.
 * @param username Student display name.
 * @param subgroupId UUID of the subgroup, or {@code null} if the student
 *                   belongs directly to the main group.
 */
@Builder
public record StudentGroupMemberResponse(
    UUID id,
    String username,
    UUID subgroupId
) {}
