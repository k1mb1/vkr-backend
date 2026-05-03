package com.github.k1mb1.vkr_backend.education.assignments.api.responses;
import java.time.Instant;
import java.util.UUID;
public record TaskResponse(UUID id, UUID lessonId, String title, String description, int maxPoints, int position, boolean isMandatory, Instant deadline, Instant createdAt, Instant updatedAt) {}
