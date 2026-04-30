package com.github.k1mb1.vkr_backend.domain.student_groups.responses;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentGroupMemberResponse;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Flat group response.
 *
 * <p>Front-end reconstructs the tree by matching {@code students} via
 * {@code subgroupId} against the {@code subgroups} array.
 */
@Builder
public record GroupResponse(
    UUID id,
    String name,
    List<SubgroupResponse> subgroups,
    List<StudentGroupMemberResponse> students
) {}
