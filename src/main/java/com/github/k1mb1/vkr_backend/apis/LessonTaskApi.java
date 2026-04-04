package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/lessons/{lessonId}/tasks",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Lesson Tasks", description = "Manage tasks (assignments) within a lesson")
public interface LessonTaskApi {

    @Operation(summary = "List all tasks for a lesson, ordered by position")
    @GetMapping
    ResponseEntity<List<TaskResponse>> findAll(@PathVariable UUID lessonId);

    @Operation(summary = "Create a new task for a lesson")
    @PostMapping
    ResponseEntity<TaskResponse> create(
        @PathVariable UUID lessonId,
        @RequestBody @Valid CreateTaskRequest request
    );

    /**
     * Partial update.  The teacher uses this to:
     * <ul>
     *   <li>advance {@code issuedTaskIndex} when issuing a newer task, which
     *       triggers recalculation of displacement coefficients on the front-end;</li>
     *   <li>change {@code penaltyMode} / {@code penaltyStep} for the lesson.</li>
     * </ul>
     */
    @Operation(summary = "Update task fields (partial)")
    @PatchMapping("/{taskId}")
    ResponseEntity<TaskResponse> update(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid UpdateTaskRequest request
    );

    @Operation(summary = "Delete a task from a lesson")
    @DeleteMapping("/{taskId}")
    ResponseEntity<Void> delete(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId
    );
}
