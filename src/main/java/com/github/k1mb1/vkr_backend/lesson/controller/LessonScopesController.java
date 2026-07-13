package com.github.k1mb1.vkr_backend.lesson.controller;

import com.github.k1mb1.vkr_backend.lesson.service.LessonScopeService;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.response.LessonScopeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lesson scopes", description = "Операции над scope занятия")
@RestController
@RequiredArgsConstructor
public class LessonScopesController {

    final LessonScopeService lessonScopeService;

    @Operation(summary = "Массовое добавление scope'ов к занятию")
    @PostMapping(value = "/api/lessons/{lessonId}/scopes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LessonScopeResponse>> addLessonScopes(
            @Parameter(description = "ID занятия") @PathVariable UUID lessonId,
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Список новых scope'ов",
                            required = true)
                    BulkAddLessonScopesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonScopeService.addScopes(lessonId, request));
    }

    @Operation(
            summary = "Удалить один scope занятия",
            description = "Помечает scope как архивный (soft-delete). Идентифицируется по глобальному ID scope'а.")
    @DeleteMapping(value = "/api/lesson-scopes/{scopeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteLessonScope(
            @Parameter(description = "ID scope'а занятия") @PathVariable UUID scopeId) {
        lessonScopeService.deleteScope(scopeId);
        return ResponseEntity.noContent().build();
    }
}
