package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SubgroupResponse(
    UUID id,
    String name,
    List<StudentEntryResponse> students
) {}
