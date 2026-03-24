package com.github.k1mb1.vkr_backend.domain.subjects.requests;

import java.time.Instant;

public record UpdateSubjectRequest(String name, String description, Boolean archived, Instant archivedAt) {}
