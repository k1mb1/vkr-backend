package com.github.k1mb1.vkr_backend.lesson.api;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Студент в аудитории занятия/проведения — публичная проекция для модулей,
 * строящих таблицы по аудитории (посещаемость, оценки, check-in).
 */
public record LessonStudentResponse(
        UUID id,
        String username,
        UUID groupId,
        String groupName,
        @Nullable UUID subgroupId,
        @Nullable Integer subgroupIndex) {}
