package com.github.k1mb1.vkr_backend.student_groups.web.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * Update request for a group and its students.
 * Existing students should be sent with id, new students with null id.
 */
public record UpdateGroupRequest(
    @NotBlank String groupName,
    @NotEmpty List<@Valid StudentPatchRequest> students
) {
    public record StudentPatchRequest(
        UUID id,
        @NotBlank String username,
        UUID subgroupId
    ) {}
}
