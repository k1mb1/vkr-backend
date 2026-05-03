package com.github.k1mb1.vkr_backend.education.lessons.api;

import java.util.UUID;

public record LessonFilterRequest(UUID subjectId, LessonType lessonType, UUID groupId) {}
