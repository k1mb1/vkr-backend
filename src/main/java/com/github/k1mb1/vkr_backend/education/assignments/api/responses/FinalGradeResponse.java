package com.github.k1mb1.vkr_backend.education.assignments.api.responses;
import java.util.UUID;
public record FinalGradeResponse(UUID studentId, String username, double earnedPoints, double maxPoints, Double percentage) {}
