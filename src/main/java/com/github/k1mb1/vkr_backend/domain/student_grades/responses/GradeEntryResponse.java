package com.github.k1mb1.vkr_backend.domain.student_grades.responses;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GradeEntryResponse(
    UUID lessonId,
    String lessonName,
    LessonType lessonType,
    OffsetDateTime lessonDateTime,
    Integer gradeValue,
    String gradeComment
) {}
