package com.github.k1mb1.vkr_backend.education.assignments.api.requests;
import java.time.Instant;
public record UpdateTaskRequest(String title, String description, Integer maxPoints, Integer position, Boolean isMandatory, Instant deadline) {}
