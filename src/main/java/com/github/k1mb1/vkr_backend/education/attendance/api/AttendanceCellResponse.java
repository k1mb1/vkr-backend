package com.github.k1mb1.vkr_backend.education.attendance.api;
import java.util.UUID;
public record AttendanceCellResponse(UUID id, UUID lessonId, UUID studentId, PresenceType presence, String note) {}
