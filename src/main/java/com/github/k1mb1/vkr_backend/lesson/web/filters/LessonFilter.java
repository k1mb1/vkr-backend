package com.github.k1mb1.vkr_backend.lesson.web.filters;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LessonFilter(UUID subjectId) {}
