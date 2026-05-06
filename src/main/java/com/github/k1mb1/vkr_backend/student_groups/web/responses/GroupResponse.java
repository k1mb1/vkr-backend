package com.github.k1mb1.vkr_backend.student_groups.web.responses;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record GroupResponse(
        UUID id,
        String name,
        List<SubgroupResponse> subgroups,
        List<StudentGroupMemberResponse> students
) {}
