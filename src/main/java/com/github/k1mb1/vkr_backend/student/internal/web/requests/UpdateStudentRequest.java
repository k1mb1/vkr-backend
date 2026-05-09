package com.github.k1mb1.vkr_backend.student.internal.web.requests;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UpdateStudentRequest(
    @NotBlank String username,
    UUID subgroupId,
    Boolean archived
) {}
