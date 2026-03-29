package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import java.util.UUID;

public record StudentEntry(
    UUID id,
    String username
) {}
