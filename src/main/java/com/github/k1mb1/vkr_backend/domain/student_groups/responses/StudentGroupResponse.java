package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import java.util.List;
import java.util.UUID;

public record StudentGroupResponse(
    UUID id,
    String name,
    /** Students assigned directly to this group (not to any subgroup). */
    List<String> students,
    List<SubgroupResponse> subgroups
) {}
