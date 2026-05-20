package com.github.k1mb1.vkr_backend.grade.web;

import com.github.k1mb1.vkr_backend.grade.AssignmentApi;
import com.github.k1mb1.vkr_backend.grade.web.requests.CreateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grade.web.requests.UpdateAssignmentRequest;
import com.github.k1mb1.vkr_backend.grade.web.responses.AssignmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
    value = "/api/assignments", produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Assignments", description = "Задания")
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    final AssignmentApi assignmentApi;

    @Operation(summary = "Создать задание")
    @PostMapping
    public ResponseEntity<AssignmentResponse> create(
        @Valid @RequestBody CreateAssignmentRequest request
    ) {
        return ResponseEntity.ok(assignmentApi.create(request));
    }

    @Operation(summary = "Получить задание по ID")
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(assignmentApi.findById(id));
    }

    @Operation(summary = "Обновить задание")
    @PatchMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateAssignmentRequest request
    ) {
        return ResponseEntity.ok(assignmentApi.update(id, request));
    }

    @Operation(summary = "Удалить задание")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        assignmentApi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
