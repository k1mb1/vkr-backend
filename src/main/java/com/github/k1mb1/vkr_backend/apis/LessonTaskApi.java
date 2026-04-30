package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.CreateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.requests.UpdateTaskRequest;
import com.github.k1mb1.vkr_backend.domain.lesson_tasks.responses.TaskResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeTableResponse;
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

    @Operation(summary = "All task grades for a lesson grouped by student")
    @GetMapping("/grades")
    ResponseEntity<GradeTableResponse> findGrades(@PathVariable UUID lessonId);

    @Operation(summary = "Upsert a student grade for a task")
    @PutMapping("/{taskId}/grades")
    ResponseEntity<GradeResponse> upsertGrade(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid UpsertTaskGradeRequest request
    );

    @Operation(
        summary = "Bulk upsert grades for a task",
        description = "Upserts grades for multiple students in a single request. Processes each entry independently; the response preserves input order."
    )
    @PutMapping("/{taskId}/grades/bulk")
    ResponseEntity<List<GradeResponse>> upsertGradesBulk(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid List<UpsertTaskGradeRequest> requests
    );
}
