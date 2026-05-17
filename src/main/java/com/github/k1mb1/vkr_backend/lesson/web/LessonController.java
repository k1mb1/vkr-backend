package com.github.k1mb1.vkr_backend.lesson.web;

import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(
    value = "/api/lessons", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Lessons", description = "Управление занятиями и расписанием")
@RestController
@RequiredArgsConstructor
public class LessonController {

    final LessonApi lessonApi;

    @Operation(
        summary = "Получить список занятий по permissionId преподавателя"
    )
    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessons(
        @ParameterObject
        @Valid
        @ModelAttribute
        LessonFilter filter
    ) {
        return ResponseEntity.ok(lessonApi.getLessons(filter));
    }

    @Operation(summary = "Частично обновить занятие")
    @PatchMapping("/{id}")
    public ResponseEntity<LessonResponse> updateLesson(
        @Parameter(description = "ID занятия")
        @PathVariable
        UUID id,
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для обновления занятия", required = true
        )
        UpdateLessonRequest request
    ) {
        return ResponseEntity.ok(lessonApi.updateLesson(id, request));
    }

    @Operation(summary = "Удалить занятие")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
        @Parameter(description = "ID занятия")
        @PathVariable
        UUID id
    ) {
        lessonApi.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Массовое создание занятий по недельному шаблону")
    @PostMapping("/bulk-schedule")
    public ResponseEntity<List<LessonResponse>> bulkScheduleLessons(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Шаблон расписания", required = true
        )
        BulkScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(lessonApi.bulkScheduleLessons(request));
    }

    @Operation(summary = "Создать занятия по количеству типов")
    @PostMapping("/by-type")
    public ResponseEntity<List<LessonResponse>> createLessonsByType(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Количество занятий по типам", required = true
        )
        CreateLessonsByTypeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(lessonApi.createLessonsByType(request));
    }
}
