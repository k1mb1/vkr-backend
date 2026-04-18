package com.github.k1mb1.vkr_backend.domain.students.filters;

import java.util.UUID;

public record FindStudentsFilter(
    String username,
    UUID groupId
) {}
