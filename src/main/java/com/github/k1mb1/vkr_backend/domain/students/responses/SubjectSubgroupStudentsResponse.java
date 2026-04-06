package com.github.k1mb1.vkr_backend.domain.students.responses;

import java.util.List;
import java.util.UUID;

public record SubjectSubgroupStudentsResponse(
    UUID id,
    String name,
    List<String> studentNames
) {}
