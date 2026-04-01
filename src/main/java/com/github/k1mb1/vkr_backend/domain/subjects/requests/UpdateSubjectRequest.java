package com.github.k1mb1.vkr_backend.domain.subjects.requests;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UpdateSubjectRequest(String name, String description, Boolean archived, Instant archivedAt) {}
