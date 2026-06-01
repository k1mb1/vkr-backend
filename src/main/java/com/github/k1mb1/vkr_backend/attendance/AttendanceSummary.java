package com.github.k1mb1.vkr_backend.attendance;

/**
 * Сводка посещаемости студента по набору проведений (lessonScope): количество по каждому статусу.
 */
public record AttendanceSummary(
    int present,
    int late,
    int absent,
    int excused
) {}
