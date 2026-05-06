package com.github.k1mb1.vkr_backend.teachers.web.requests;

public record CreateOrUpdateTeacherRequest(
        String username,
        String email
) {
}