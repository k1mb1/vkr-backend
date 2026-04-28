package com.github.k1mb1.vkr_backend.domain.teachers.requests;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;

/**
 * DTO for {@link TeacherEntity}
 */
public record UpdateTeacherRequest(String username, String email) {}
//TODO сделай что это не update а create, а для update сделай отдельный реквест, который будет содержать только те поля, которые можно менять, например не username, а только email