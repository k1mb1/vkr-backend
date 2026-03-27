package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Request to bulk-generate lessons for a subject using recurring schedules.
 *
 * <p>Each entry in {@code schedules} independently describes one recurrence rule
 * for a given lesson type. You can mix lecture and practice entries freely:
 *
 * <pre>
 * {
 *   "subjectId": "...",
 *   "schedules": [
 *     {
 *       "type": "LECTURE",
 *       "recurrence": "WEEKLY",
 *       "daysOfWeek": ["MONDAY"],
 *       "time": "09:00",
 *       "startDate": "2025-09-01",
 *       "intervalWeeks": 1,
 *       "endDate": "2026-01-31"
 *     },
 *     {
 *       "type": "PRACTICE",
 *       "recurrence": "WEEKLY",
 *       "daysOfWeek": ["WEDNESDAY", "FRIDAY"],
 *       "time": "11:00",
 *       "startDate": "2025-09-03",
 *       "intervalWeeks": 2,
 *       "totalCount": 10
 *     },
 *     {
 *       "comment": "Second lecture on the same Monday at a different time",
 *       "type": "LECTURE",
 *       "recurrence": "WEEKLY",
 *       "daysOfWeek": ["MONDAY"],
 *       "time": "13:00",
 *       "startDate": "2025-09-01",
 *       "intervalWeeks": 1,
 *       "endDate": "2026-01-31"
 *     }
 *   ]
 * }
 * </pre>
 */
public record BulkScheduleLessonsRequest(
    @NotNull UUID subjectId,
    @NotEmpty List<@Valid LessonScheduleEntry> schedules
) {}
