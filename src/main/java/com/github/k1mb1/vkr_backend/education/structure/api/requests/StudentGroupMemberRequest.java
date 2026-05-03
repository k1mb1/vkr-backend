package com.github.k1mb1.vkr_backend.education.structure.api.requests;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
public record StudentGroupMemberRequest(@NotBlank String username, @Min(0) Integer subgroupIndex) {}
