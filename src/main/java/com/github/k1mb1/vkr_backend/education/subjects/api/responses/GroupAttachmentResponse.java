package com.github.k1mb1.vkr_backend.education.subjects.api.responses;
import java.util.UUID;
public record GroupAttachmentResponse(UUID subjectId, String subjectName, UUID groupId, String groupName) {}
