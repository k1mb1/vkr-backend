package com.github.k1mb1.vkr_backend.education.assignments.api;

import com.github.k1mb1.vkr_backend.education.assignments.api.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.FinalGradeResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.FindGradesFilter;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.GradeMatrixResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.GradeResponse;
import com.github.k1mb1.vkr_backend.education.assignments.api.responses.GradeTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Grades", description = "Grade management")
public interface GradeApi {
    @Operation(summary = "Grade matrix for a subject")
    @GetMapping("/subjects/{subjectId}/grades")
    ResponseEntity<GradeMatrixResponse> findGradesBySubjectId(
        @PathVariable UUID subjectId,
        @ParameterObject @ModelAttribute FindGradesFilter filter
    );

    @Operation(summary = "Grade table for a lesson (all tasks × all students)")
    @GetMapping("/lessons/{lessonId}/grades")
    ResponseEntity<GradeTableResponse> findGradesByLesson(@PathVariable UUID lessonId);

    @Operation(summary = "Final weighted grades for a subject")
    @GetMapping("/subjects/{subjectId}/grades/final")
    ResponseEntity<List<FinalGradeResponse>> computeFinalGrades(@PathVariable UUID subjectId);

    @Operation(summary = "List grades for a task with optional filter")
    @GetMapping("/lessons/{lessonId}/tasks/{taskId}/grades")
    ResponseEntity<Page<GradeResponse>> findGrades(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @ParameterObject @ModelAttribute GradeFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "Upsert a student grade for a task")
    @PutMapping("/lessons/{lessonId}/tasks/{taskId}/grades")
    ResponseEntity<GradeResponse> upsert(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid UpsertTaskGradeRequest request
    );

    @Operation(summary = "Bulk upsert student grades for a task")
    @PutMapping("/lessons/{lessonId}/tasks/{taskId}/grades/bulk")
    ResponseEntity<List<GradeResponse>> upsertBulk(
        @PathVariable UUID lessonId,
        @PathVariable UUID taskId,
        @RequestBody @Valid List<UpsertTaskGradeRequest> requests
    );
}
