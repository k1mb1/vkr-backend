package com.github.k1mb1.vkr_backend.student_groups.web.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateGroupRequest(
        @NotBlank String groupName,
        @NotEmpty List<@Valid StudentGroupMemberRequest> students
) {

    /**
     * Student entry for group creation.
     *
     * @param username Student display name.
     * @param subgroupIndex 0-based subgroup index. {@code null} means the student
     *                      is placed directly in the main group (no subgroup).
     */
    public record StudentGroupMemberRequest(
            @NotBlank String username,
            @Min(0) Integer subgroupIndex
    ) {}
}
