package com.github.k1mb1.vkr_backend.group.web.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UpdateGroupRequest(
    @NotBlank String groupName,
    @NotEmpty List<@Valid StudentPatchRequest> students
) {
    @Builder
    public record StudentPatchRequest(
        UUID id,
        @NotBlank String username,
        UUID subgroupId
    ) {}
}
