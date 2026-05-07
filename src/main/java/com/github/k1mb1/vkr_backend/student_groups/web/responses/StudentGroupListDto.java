package com.github.k1mb1.vkr_backend.student_groups.web.responses;

import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentGroupListDto(
    UUID id,
    String name,
    int subgroupCount,
    int totalStudentCount
) {}
