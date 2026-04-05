package com.github.k1mb1.vkr_backend.domain.lessons.requests;

public enum RecurrenceType {
    /**
     * Repeating weekly (or every N weeks when {@code intervalWeeks > 1}).
     * Requires {@code intervalWeeks} on the enclosing {@link LessonScheduleEntry}.
     */
    WEEKLY,

    /**
     * One-shot lesson: only the slots whose date equals {@code startDate} are created.
     * {@code totalCount} is still respected as a hard cap (usually 1 per slot),
     * and {@code intervalWeeks} / {@code weekIndex} are ignored.
     */
    ONCE,
}
