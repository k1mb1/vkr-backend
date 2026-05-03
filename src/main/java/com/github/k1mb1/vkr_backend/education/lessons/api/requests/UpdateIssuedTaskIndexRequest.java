package com.github.k1mb1.vkr_backend.education.lessons.api.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateIssuedTaskIndexRequest(
    @NotNull @Min(0) Integer issuedTaskIndex
) {}
