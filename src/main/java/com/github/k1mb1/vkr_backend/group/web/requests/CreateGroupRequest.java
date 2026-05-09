package com.github.k1mb1.vkr_backend.group.web.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateGroupRequest(
    @NotBlank String groupName,
    @NotEmpty List<@Valid StudentGroupMemberRequest> students
) {}
