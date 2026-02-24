package com.github.k1mb1.vkr_backend.domain.teachers.responses;

import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link TeacherEntity}
 */
public record TeacherDetailsResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String username,
        String email,
        Set<SubjectResponse> subjects
) {
}