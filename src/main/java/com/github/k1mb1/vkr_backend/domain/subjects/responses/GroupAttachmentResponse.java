package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import java.util.UUID;

public record GroupAttachmentResponse(
    UUID subjectId,
    String subjectName,
    UUID groupId,
    String groupName
) {}
