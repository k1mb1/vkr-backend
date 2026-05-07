package com.github.k1mb1.vkr_backend.student_groups.web.filters;

import lombok.Builder;

@Builder
public record StudentGroupFilterRequest(String name) {}
