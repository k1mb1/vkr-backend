package com.github.k1mb1.vkr_backend.attendance.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Занятие в таблице посещаемости")
public record AttendanceTableLesson(
    @Schema(description = "ID занятия") UUID id,

    @Schema(description = "Время начала") Instant startedAt,

    @Schema(description = "Тип занятия") LessonType type,

    @Schema(description = "Тема") String topic
) {}
