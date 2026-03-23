package com.github.k1mb1.vkr_backend.domain.lessons.requests;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateLessonRequest(
    @NotBlank String name,
    Instant dateTime,
    @NotNull LessonType type,
    @NotNull UUID subjectId
) {}
