package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Request to bulk-generate lessons for a subject using recurring schedules.
 *
 * <p>Each entry combines all lesson types (lectures + practices) within one cycle,
 * so a single entry can describe both a Monday lecture and a Wednesday practice
 * repeating every 2 weeks — no need for separate entries per type.
 *
 * <pre>
 * {
 *   "subjectId": "...",
 *   "schedules": [
 *     {
 *       "recurrence": "WEEKLY",
 *       "intervalWeeks": 2,
 *       "startDate": "2025-09-01",
 *       "totalCount": 16,
 *       "slots": [
 *         { "type": "LECTURE",  "weekIndex": 0, "daysOfWeek": ["MONDAY"],    "time": "09:00" },
 *         { "type": "LECTURE",  "weekIndex": 1, "daysOfWeek": ["MONDAY"],    "time": "09:00" },
 *         { "type": "PRACTICE", "weekIndex": 1, "daysOfWeek": ["WEDNESDAY"], "time": "11:00" }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 */
public record BulkScheduleLessonsRequest(
    @NotNull UUID subjectId,
    @NotEmpty List<@Valid LessonScheduleEntry> schedules
) {}
