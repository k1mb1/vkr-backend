package com.github.k1mb1.vkr_backend.education.assignments.api.responses;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import java.util.UUID;
public record FindGradesFilter(LessonType lessonType, UUID groupId) {}
