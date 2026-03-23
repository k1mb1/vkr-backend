package com.github.k1mb1.vkr_backend.domain.student_groups.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateStudentGroupRequest(@NotBlank String name) {}
