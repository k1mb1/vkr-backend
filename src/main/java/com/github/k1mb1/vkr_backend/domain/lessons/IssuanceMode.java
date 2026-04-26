package com.github.k1mb1.vkr_backend.domain.lessons;

/**
 * Controls when a lesson becomes visible and accessible to students.
 *
 * <ul>
 *   <li><b>AUTO</b> — the lesson is issued automatically when the current time
 *       reaches {@code lesson.dateTime}. No teacher action required.</li>
 *   <li><b>MANUAL</b> — the teacher explicitly issues the lesson via
 *       {@code POST /api/lessons/{id}/issue}. The timestamp is recorded in
 *       {@code lesson.issuedAt}.</li>
 * </ul>
 */
public enum IssuanceMode {
    AUTO,
    MANUAL,
}
