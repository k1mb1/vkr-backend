package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.student.StudentResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupResponse(
    UUID id,
    String name,
    List<SubgroupResponse> subgroups,
    List<StudentResponse> students,
    Instant createdAt,
    Instant updatedAt
) {}
