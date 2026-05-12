package com.github.k1mb1.vkr_backend.lesson.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LessonType", description = "Тип занятия")
public enum LessonType {
    NONE,
    LECTURE,
    PRACTICE,
}
