package com.github.k1mb1.vkr_backend.domain.subjects.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Assigns students to a subject in bulk using a nested list structure.
 *
 * <p>The outer list index determines subgroup placement:
 * <ul>
 *   <li>If only one inner list is provided — all students go into the main group, no subgroups.</li>
 *   <li>If multiple inner lists are provided — each list becomes subgroup N+1
 *       (index 0 → subgroup "ИСТ-21/1", index 1 → subgroup "ИСТ-21/2", etc.).</li>
 * </ul>
 *
 * <p>Example — no subgroups:
 * <pre>{@code
 * { "groupName": "ИСТ-21", "usernames": [["ivanov", "petrov", "sidorov"]] }
 * }</pre>
 *
 * <p>Example — two subgroups:
 * <pre>{@code
 * { "groupName": "ИСТ-21", "usernames": [["ivanov", "petrov"], ["sidorov", "kozlov"]] }
 * }</pre>
 */
public record AddStudentsByGroupRequest(

    /** Name of the main group (e.g. "ИСТ-21"). Created if it does not exist. */
    @NotBlank String groupName,

    /**
     * Nested list of usernames. Each inner list represents one subgroup (by index).
     * A single inner list means no subgroups — everyone goes to the main group.
     */
    @NotEmpty List<@NotEmpty List<@NotBlank String>> usernames

) {}
