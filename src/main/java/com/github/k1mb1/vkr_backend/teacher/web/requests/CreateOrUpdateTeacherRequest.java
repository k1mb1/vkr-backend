package com.github.k1mb1.vkr_backend.teacher.web.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateTeacherRequest(
    @NotBlank String username,
    @NotBlank @Email String email
) {}
