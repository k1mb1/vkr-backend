package com.github.k1mb1.vkr_backend.journal.controller;

import com.github.k1mb1.vkr_backend.journal.service.GradingService;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.request.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.journal.service.dto.response.AssignmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/api/assignments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Assignments", description = "Задания для оценивания")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    final GradingService gradingService;

    @Operation(summary = "Получить список заданий по lessonId (отсортированный по order)")
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByLesson(
            @Parameter(description = "ID занятия") @RequestParam UUID lessonId) {
        return ResponseEntity.ok(gradingService.getAssignmentsByLesson(lessonId));
    }

    @Operation(summary = "Создать список заданий для урока (только если у урока ещё нет заданий)")
    @PostMapping
    public ResponseEntity<List<AssignmentResponse>> createAssignments(
            @Valid @RequestBody
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Список заданий для урока",
                            required = true)
                    CreateAssignmentsRequest request) {
        return ResponseEntity.ok(gradingService.createAssignments(request));
    }

    @Operation(summary = "Массовое обновление заданий урока")
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<List<AssignmentResponse>> updateLessonAssignments(
            @Parameter(description = "ID занятия") @PathVariable UUID lessonId,
            @Valid @RequestBody BulkUpdateAssignmentsRequest request) {
        return ResponseEntity.ok(gradingService.updateAssignmentsOfLesson(lessonId, request));
    }

    @Operation(summary = "Удалить задание (связанные оценки удаляются каскадно)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@Parameter(description = "ID задания") @PathVariable UUID id) {
        gradingService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить все задания занятия (связанные оценки удаляются каскадно)")
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLessonAssignments(
            @Parameter(description = "ID занятия") @PathVariable UUID lessonId) {
        gradingService.deleteAssignmentsOfLesson(lessonId);
        return ResponseEntity.noContent().build();
    }
}
