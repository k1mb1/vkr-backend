package com.github.k1mb1.vkr_backend.education.attendance.api;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import java.util.UUID;
public record FindAttendanceFilter(LessonType lessonType, UUID groupId) {}
