package com.github.k1mb1.vkr_backend.education.assignments.api.requests;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
public record CreateTaskRequest(@NotBlank String title, String description, @NotNull @Min(0) Integer maxPoints, int position, boolean isMandatory, Instant deadline) {}
