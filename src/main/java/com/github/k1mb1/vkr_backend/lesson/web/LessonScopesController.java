package com.github.k1mb1.vkr_backend.lesson.web;

import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Lesson scopes", description = "Операции над scope занятия")
@RestController
@RequiredArgsConstructor
public class LessonScopesController {

    final LessonScopesApi lessonScopesApi;

    @Operation(summary = "Массовое добавление scope'ов к занятию")
    @PostMapping(
        value = "/api/lessons/{lessonId}/scopes", produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<LessonScopeResponse>> addScopes(
        @Parameter(description = "ID занятия")
        @PathVariable
        UUID lessonId,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Список новых scope'ов", required = true
        )
        BulkAddLessonScopesRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(lessonScopesApi.addScopes(lessonId, request));
    }
}
