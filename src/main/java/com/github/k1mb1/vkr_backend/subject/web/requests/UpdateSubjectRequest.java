package com.github.k1mb1.vkr_backend.subject.web.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateSubjectRequest(
    @NotBlank String name,
    String description,
    Boolean archived
) {}
