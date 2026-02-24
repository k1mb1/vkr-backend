package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link SubjectEntity}
 */
public record SubjectDetailsResponse(
        UUID id, Instant createdAt,
        Instant updatedAt, String name,
        String description,
        Set<TeacherResponse> teachers,
        Set<StudentResponse> students,
        Set<LessonResponse> lessons
) {
}