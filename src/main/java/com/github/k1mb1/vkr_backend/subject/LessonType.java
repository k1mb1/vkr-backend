package com.github.k1mb1.vkr_backend.subject;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип занятия")
public enum LessonType {
    LECTURE,
    PRACTICE,
}
