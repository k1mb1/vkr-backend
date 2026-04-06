package com.github.k1mb1.vkr_backend.domain.students.responses;

import java.util.List;
import java.util.UUID;

public record StudentSubjectSubgroupsResponse(
    UUID subjectId,
    String subjectName,
    List<SubjectSubgroupStudentsResponse> subgroups
) {}
