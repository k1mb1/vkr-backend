package com.github.k1mb1.vkr_backend.education.structure.api.responses;
import java.time.Instant;
import java.util.UUID;
public record StudentResponse(UUID id, String username, UUID groupId, String groupName, Instant createdAt, Instant updatedAt) {}
