package com.github.k1mb1.vkr_backend.domain.student_groups.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request to create a group with students in one call.
 *
 * <p>One inner list  → all students go directly into the main group, no subgroups.
 * <p>N inner lists → subgroups "groupName/1" … "groupName/N" are auto-created;
 *    each inner list populates the corresponding subgroup.
 *
 * <pre>
 * // No subgroups
 * { "groupName": "ИСТ-21", "studentNames": [["ivanov","petrov"]] }
 *
 * // Two subgroups
 * { "groupName": "ИСТ-21", "studentNames": [["ivanov","petrov"],["sidorov","kozlov"]] }
 * </pre>
 */
public record CreateGroupRequest(

    @NotBlank
    String groupName,

    /** Outer index = subgroup number (1-based). Single inner list = no subgroups. */
    @NotEmpty
    List<@NotNull List<@NotBlank String>> studentNames

) {}
