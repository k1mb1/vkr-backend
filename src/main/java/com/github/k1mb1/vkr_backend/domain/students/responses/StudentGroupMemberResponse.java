package com.github.k1mb1.vkr_backend.domain.students.responses;

import java.util.UUID;

/**
 * Student entry inside a group response.
 *
 * @param id         Student UUID.
 * @param username   Student display name.
 * @param subgroupId UUID of the subgroup, or {@code null} if the student
 *                   belongs directly to the main group.
 */
public record StudentGroupMemberResponse(
    UUID id,
    String username,
    UUID subgroupId
) {}
