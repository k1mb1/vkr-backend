package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentTaskGradesResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.TaskGradeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/lessons/{lessonId}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(
    name = "Student Task Grades",
    description = "Grades for lesson task assignments"
)
public interface StudentTaskGradeApi {
    @Operation(summary = "All task grades for a lesson grouped by student")
    @GetMapping("/task-grades")
    ResponseEntity<List<StudentTaskGradesResponse>> findByLesson(
        @PathVariable UUID lessonId
    );

    @Operation(
        summary = "Upsert a student grade for a task",
        description = "Creates or updates the grade for the given (task, student) pair. " +
            "Pass submittedAt to record submission time."
    )
    @PutMapping("/tasks/{taskId}/grades")
    ResponseEntity<TaskGradeResponse> upsert(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid UpsertTaskGradeRequest request
    );
}
