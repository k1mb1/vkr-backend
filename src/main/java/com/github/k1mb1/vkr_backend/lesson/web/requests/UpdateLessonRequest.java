package com.github.k1mb1.vkr_backend.lesson.web.requests;

import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpdateAssignmentsRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(
    description = """
        Полное обновление занятия одним запросом.
        Любая секция null или отсутствует — соответствующая часть не трогается.
        Транзакция атомарная: при ошибке в любой из секций откатываются все.
        """
)
public record UpdateLessonRequest(
    @Valid
    @Schema(description = "Шапка занятия (subject, type, topic, orderIndex). null — не трогать.")
    UpdateLessonHeaderRequest header,

    @Valid
    @Schema(description = "Замена scope'ов по id. Все scope'ы должны принадлежать этому уроку. null — не трогать.")
    BulkReplaceLessonScopesRequest scopes,

    @Valid
    @Schema(description = "Обновление заданий по id. Все задания должны принадлежать этому уроку. null — не трогать.")
    BulkUpdateAssignmentsRequest assignments
) {}
