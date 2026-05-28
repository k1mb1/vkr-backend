package com.github.k1mb1.vkr_backend.lesson.web;

import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
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
        summary = "Получить список занятий по permissionId"
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

    @Operation(summary = "Получить занятие по ID")
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(
        @Parameter(description = "ID занятия")
        @PathVariable
        UUID id
    ) {
        return ResponseEntity.ok(lessonApi.getLessonById(id));
    }

    @Operation(
        summary = "Обновить занятие целиком одним запросом",
        description = "В body можно передать любую комбинацию из header / scopes / assignments — null = не трогать. Транзакция атомарная."
    )
    @PutMapping("/{id}")
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

    @Operation(summary = "Массовое создание занятий: N лекций + M практик с allGroups-scope")
    @PostMapping("/bulk")
    public ResponseEntity<List<LessonResponse>> bulkCreate(
        @Valid
        @RequestBody
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Параметры массового создания", required = true
        )
        BulkCreateLessonsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonApi.bulkCreate(request));
    }
}
