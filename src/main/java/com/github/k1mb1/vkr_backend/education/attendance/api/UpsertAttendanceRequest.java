package com.github.k1mb1.vkr_backend.education.attendance.api;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record UpsertAttendanceRequest(@NotNull UUID studentId, @NotNull PresenceType presence, String note) {}
