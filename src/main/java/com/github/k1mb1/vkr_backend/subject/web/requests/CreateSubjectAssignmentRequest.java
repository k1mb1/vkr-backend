package com.github.k1mb1.vkr_backend.subject.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSubjectAssignmentRequest(
    @NotNull
    UUID teacherId,

    @NotNull
    UUID offeringId,

    UUID subgroupId,

    LessonType lessonTypeScope
) {}
