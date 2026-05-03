package com.github.k1mb1.vkr_backend.education.structure.api.requests;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record CreateStudentRequest(@NotBlank String username, UUID groupId) {}
