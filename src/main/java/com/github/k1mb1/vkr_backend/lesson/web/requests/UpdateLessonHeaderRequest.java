package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = """
        Частичное обновление шапки занятия. Поле null или отсутствует — не меняется.
        Используется как секция `header` внутри UpdateLessonRequest.
        """)
public record UpdateLessonHeaderRequest(
        @Schema(description = "ID предмета (null — не менять)")
        UUID subjectId,

        @Schema(description = "Тип занятия (null — не менять)")
        LessonType type,

        @Schema(description = "Тема занятия (null — не менять)")
        String topic,

        @Schema(description = "Порядковый номер в рамках (предмет, тип). null — не менять")
        Integer orderIndex) {}
