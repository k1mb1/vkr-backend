package com.github.k1mb1.vkr_backend.education.assignments.api.responses;
import com.github.k1mb1.vkr_backend.education.assignments.api.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;
public record GradeResponse(UUID id, UUID taskId, UUID lessonId, UUID studentId, Integer value, String comment, SubmissionStatus status, Instant submittedAt, Instant createdAt, Instant updatedAt) {}
