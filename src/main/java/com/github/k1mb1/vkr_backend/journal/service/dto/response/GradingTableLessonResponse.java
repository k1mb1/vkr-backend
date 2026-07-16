package com.github.k1mb1.vkr_backend.journal.service.dto.response;

import com.github.k1mb1.vkr_backend.subject.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

@Schema(description = "Занятие в таблице оценок (шаблон); даты — в scopes")
public record GradingTableLessonResponse(
        @Schema(description = "ID занятия") UUID id,

        @Schema(description = "Тип занятия") LessonType type,

        @Schema(description = "Порядковый номер занятия в рамках (предмет, тип)")
        int orderIndex,

        @Schema(description = "Тема") String topic,

        @Schema(description = "Активное (текущее) занятие — точка отсчёта для понижения балла")
        boolean active,

        @Schema(description = "Проведения этого занятия для соответствующих групп")
        List<Scope> scopes) {
    @Schema(
            name = "GradingTableScope",
            description = "Проведение занятия для группы (для отображения даты в шапке колонки)")
    public record Scope(
            @Schema(description = "ID проведения") UUID id,

            @Schema(description = "ID группы (null = все группы)") @Nullable UUID groupId,

            @Schema(description = "ID разрешённой подгруппы (null = вся группа)") @Nullable UUID allowedSubgroupId,

            @Schema(description = "Дата проведения") LocalDate startedAt,

            @Schema(description = "true = проведение для всех групп")
            boolean allGroups) {}
}
