package com.github.k1mb1.vkr_backend.journal.service.dto.response;

import lombok.Builder;

/**
 * Сводка посещаемости студента по набору проведений (lessonScope): количество по каждому статусу.
 */
@Builder
public record AttendanceSummaryResponse(int present, int late, int absent, int excused) {}
