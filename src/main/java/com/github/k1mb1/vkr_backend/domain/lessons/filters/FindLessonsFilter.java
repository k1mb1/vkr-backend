package com.github.k1mb1.vkr_backend.domain.lessons.filters;

import java.util.UUID;

public record FindLessonsFilter(
    UUID subjectId
) {}
