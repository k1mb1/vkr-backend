package com.github.k1mb1.vkr_backend.lesson.service.dto.request;

import com.github.k1mb1.vkr_backend.subject.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

        @Size(max = 2_000) @Schema(description = "Тема занятия (null — не менять)")
        String topic,

        @Min(1) @Max(1_000) @Schema(description = "Порядковый номер в рамках (предмет, тип). null — не менять")
        Integer orderIndex) {}
