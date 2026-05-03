package com.github.k1mb1.vkr_backend.education.attendance.api;
import java.util.UUID;
public record AttendanceEntryResponse(UUID id, UUID lessonId, PresenceType presence, String note) {}
