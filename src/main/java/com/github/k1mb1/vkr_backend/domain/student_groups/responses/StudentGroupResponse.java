package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import java.util.List;
import java.util.UUID;

/**
 * Response for GET /api/groups/{id}.
 *
 * Two shapes depending on whether the group has subgroups:
 *
 * No subgroups → subgroups is empty, students contains all members directly.
 * With subgroups → students is empty (no direct members), subgroups holds each
 *                  subgroup with its own student list.
 */
public record StudentGroupResponse(
    UUID id,
    String name,
    /**
     * Students assigned directly to the main group (no subgroup).
     * Empty when the group has subgroups.
     */
    List<StudentEntry> students,
    /**
     * Subgroups with their students.
     * Empty when the group has no subgroups.
     */
    List<SubgroupResponse> subgroups
) {}
