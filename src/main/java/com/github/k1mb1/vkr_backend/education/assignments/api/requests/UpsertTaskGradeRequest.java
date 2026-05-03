package com.github.k1mb1.vkr_backend.education.assignments.api.requests;
import com.github.k1mb1.vkr_backend.education.assignments.api.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
public record UpsertTaskGradeRequest(@NotNull UUID studentId, Integer value, String comment, SubmissionStatus status, Instant submittedAt) {}
