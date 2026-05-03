package com.github.k1mb1.vkr_backend.education.structure.api;
import java.util.UUID;
public record StudentMovedEvent(UUID studentId, UUID oldGroupId, UUID newGroupId) {}
