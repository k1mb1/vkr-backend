package com.github.k1mb1.vkr_backend.domain.lessons.requests;

/**
 * How a recurring lesson schedule repeats.
 */
public enum RecurrenceType {
    /** Repeat every N weeks on specific days of the week. */
    WEEKLY,

    /** Repeat every N months on specific days of the week (first matching weekday on/after anchor). */
    MONTHLY
}
