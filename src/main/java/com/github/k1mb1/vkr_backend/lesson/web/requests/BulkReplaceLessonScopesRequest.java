package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = """
        Массовая замена scope'ов занятий (PUT).
        Каждый элемент — полное целевое состояние scope с указанным id.
        audience=null означает allGroups=true; иначе — конкретная группа [+ подгруппа].
        В одном запросе можно править scope'ы разных уроков.
        """)
public record BulkReplaceLessonScopesRequest(
        @Schema(description = "Список целевых состояний", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty @Valid List<Item> items) {
    @Schema(name = "ReplaceLessonScopeItem", description = "Целевое состояние одного scope")
    public record Item(
            @Schema(description = "ID scope", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID id,

            @Schema(
                    description = "Аудитория. null = allGroups=true; иначе — конкретная группа",
                    types = {"object", "null"})
            @Valid LessonScopeAudienceRequest audience,

            @Schema(description = "Дата проведения", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull LocalDate startedAt) {}
}
