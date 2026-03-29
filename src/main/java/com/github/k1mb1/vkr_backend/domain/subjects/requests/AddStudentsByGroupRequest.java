package com.github.k1mb1.vkr_backend.domain.subjects.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Assigns students to a subject in bulk, auto-creating missing students and groups.
 *
 * <p>Flow:
 * <ol>
 *   <li>Find or create the main group by {@code groupName}.</li>
 *   <li>For each entry: find or create the student by username.</li>
 *   <li>If {@code subgroupName} is present — find or create a subgroup whose
 *       {@code parentGroup} is the main group, and assign the student there.</li>
 *   <li>If {@code subgroupName} is absent — assign the student to the main group.</li>
 *   <li>Add the student to the subject.</li>
 * </ol>
 */
public record AddStudentsByGroupRequest(

    /** Name of the main group (e.g. "ИСТ-21"). Created if it does not exist. */
    @NotBlank String groupName,

    /** List of students to assign. */
    @NotEmpty @Valid List<StudentEntry> students

) {

    public record StudentEntry(

        /** Student login / display name. */
        @NotBlank String username,

        /**
         * Optional subgroup name (e.g. "ИСТ-21/1").
         * When present the student is placed in this subgroup instead of the main group.
         * The subgroup is created automatically if it does not exist.
         */
        String subgroupName

    ) {}
}
