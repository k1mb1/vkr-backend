package com.github.k1mb1.vkr_backend.domain.student_attendances.filters;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import java.util.UUID;

/**
 * Query parameters for filtering the subject attendance table.
 *
 * @param lessonType Filter by lesson type (LECTURE, PRACTICE, NONE). Null = all types.
 * @param groupId    Filter by group/subgroup. Null = all groups.
 */
public record FindAttendanceFilter(
    LessonType lessonType,
    UUID groupId
) {}
