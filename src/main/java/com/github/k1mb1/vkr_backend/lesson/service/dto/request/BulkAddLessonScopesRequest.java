package com.github.k1mb1.vkr_backend.lesson.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Schema(description = """
        Массовое добавление scope'ов к занятию.
        Каждый элемент — новый scope; audience=null означает allGroups=true, иначе — конкретная группа.
        Все элементы и существующие scope'ы урока проверяются на пересечение в одном проходе.
        """)
public record BulkAddLessonScopesRequest(
        @Schema(description = "Список новых scope'ов", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Valid @Size(max = 1_000) List<Item> items) {
    @Schema(name = "AddLessonScopeItem", description = "Новый scope")
    public record Item(
            @Schema(
                    description = "Аудитория. null = allGroups=true; иначе — конкретная группа",
                    types = {"object", "null"})
            @Valid LessonScopeAudienceRequest audience,

            @Schema(description = "Дата проведения", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull LocalDate startedAt) {}
}
