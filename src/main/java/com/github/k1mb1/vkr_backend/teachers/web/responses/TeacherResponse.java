package com.github.k1mb1.vkr_backend.teachers.web.responses;

import java.util.UUID;

public record TeacherResponse(
        UUID id,
        String username,
        String email
) {
}