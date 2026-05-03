package com.github.k1mb1.vkr_backend.education.subjects.api.requests;
import java.time.Instant;
import lombok.Builder;
@Builder
public record UpdateSubjectRequest(String name, String description, Boolean archived, Instant archivedAt) {}
