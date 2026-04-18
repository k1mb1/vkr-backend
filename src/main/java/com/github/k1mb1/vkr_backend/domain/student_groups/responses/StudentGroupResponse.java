package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentGroupResponse(
    UUID id,
    String name,
    List<GroupSubjectResponse> subjects,
    /**
     * Students assigned directly to the main group (no subgroup).
     * Empty when the group has subgroups.
     */
    List<StudentEntryResponse> students,
    /**
     * Subgroups with their students.
     * Empty when the group has no subgroups.
     */
    List<SubgroupResponse> subgroups
) {}
