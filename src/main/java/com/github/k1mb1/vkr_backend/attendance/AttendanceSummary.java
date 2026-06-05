package com.github.k1mb1.vkr_backend.attendance;

import lombok.Builder;

/**
 * Сводка посещаемости студента по набору проведений (lessonScope): количество по каждому статусу.
 */
@Builder
public record AttendanceSummary(
    int present,
    int late,
    int absent,
    int excused
) {}
