package com.github.k1mb1.vkr_backend.domain.subjects.responses;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubjectLessonTableEntryResponse(
    UUID lessonId,
    String lessonName,
    OffsetDateTime dateTime
) {}
