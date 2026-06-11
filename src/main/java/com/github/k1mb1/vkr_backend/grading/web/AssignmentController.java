package com.github.k1mb1.vkr_backend.grading.web;

import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpdateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/assignments",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Assignments", description = "Задания для оценивания")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    final GradingApi gradingApi;

    @Operation(
        summary = "Получить список заданий по lessonId (отсортированный по order)"
    )
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> getByLesson(
        @RequestParam UUID lessonId
    ) {
        return ResponseEntity.ok(gradingApi.getAssignmentsByLesson(lessonId));
    }

    @Operation(
        summary = "Создать список заданий для урока (только если у урока ещё нет заданий)"
    )
    @PostMapping
    public ResponseEntity<List<AssignmentResponse>> create(
        @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Список заданий для урока",
            required = true
        ) CreateAssignmentsRequest request
    ) {
        return ResponseEntity.ok(gradingApi.createAssignments(request));
    }

    @Operation(summary = "Массовое обновление заданий урока")
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<List<AssignmentResponse>> updateByLesson(
        @PathVariable UUID lessonId,
        @Valid @RequestBody BulkUpdateAssignmentsRequest request
    ) {
        return ResponseEntity.ok(
            gradingApi.updateAssignmentsOfLesson(lessonId, request)
        );
    }

    @Operation(
        summary = "Удалить задание (связанные оценки удаляются каскадно)"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        gradingApi.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Удалить все задания занятия (связанные оценки удаляются каскадно)"
    )
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteAllOfLesson(@PathVariable UUID lessonId) {
        gradingApi.deleteAssignmentsOfLesson(lessonId);
        return ResponseEntity.noContent().build();
    }
}
