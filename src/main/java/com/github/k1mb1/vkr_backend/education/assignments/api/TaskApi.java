package com.github.k1mb1.vkr_backend.education.assignments.api;

import com.github.k1mb1.vkr_backend.education.assignments.api.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/lessons/{lessonId}/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tasks", description = "Lesson task management")
public interface TaskApi {
    @Operation(summary = "List tasks for a lesson with optional filter")
    @GetMapping
    ResponseEntity<List<TaskResponse>> findAll(
        @PathVariable UUID lessonId,
        @ParameterObject @ModelAttribute TaskFilter filter
    );

    @Operation(summary = "Create a task for a lesson")
    @PostMapping
    ResponseEntity<TaskResponse> create(
        @PathVariable UUID lessonId,
        @RequestBody @Valid CreateTaskRequest request
    );

    @Operation(summary = "Update a task")
    @PatchMapping("/{taskId}")
    ResponseEntity<TaskResponse> update(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid UpdateTaskRequest request
    );

    @Operation(summary = "Delete a task")
    @DeleteMapping("/{taskId}")
    ResponseEntity<Void> delete(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId
    );
}
