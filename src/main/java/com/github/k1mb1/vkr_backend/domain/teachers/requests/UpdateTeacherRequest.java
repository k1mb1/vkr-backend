package com.github.k1mb1.vkr_backend.domain.teachers.requests;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;

/**
 * DTO for {@link TeacherEntity}
 */
public record UpdateTeacherRequest(String username, String email) {}
