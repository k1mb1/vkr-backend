package com.github.k1mb1.vkr_backend.student.internal.web.requests;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateStudentRequest(
    @NotBlank String username,
    UUID groupId,
    UUID subgroupId
) {}
