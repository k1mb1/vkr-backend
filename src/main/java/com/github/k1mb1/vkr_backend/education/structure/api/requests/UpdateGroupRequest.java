package com.github.k1mb1.vkr_backend.education.structure.api.requests;
import jakarta.validation.constraints.NotBlank;
public record UpdateGroupRequest(@NotBlank String name) {}
