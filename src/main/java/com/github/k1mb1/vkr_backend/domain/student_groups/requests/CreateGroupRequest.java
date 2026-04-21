package com.github.k1mb1.vkr_backend.domain.student_groups.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateGroupRequest(
    @NotBlank String groupName,
    @NotEmpty List<@NotNull List<@NotBlank String>> studentNames
) {}
