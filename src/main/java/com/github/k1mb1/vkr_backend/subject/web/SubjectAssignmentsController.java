package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectAssignmentApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateSubjectAssignmentRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectAssignmentRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectAssignmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/subject-assignments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subject Assignments", description = "Teacher–Offering bindings")
@RequiredArgsConstructor
public class SubjectAssignmentsController {

    final SubjectAssignmentApi subjectAssignmentApi;

    @Operation(summary = "Assign a teacher to a subject offering (optionally scoped to subgroup / lesson type)")
    @PostMapping
    public ResponseEntity<SubjectAssignmentResponse> create(
        @Valid
        @RequestBody
        CreateSubjectAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectAssignmentApi.create(request));
    }

    @Operation(summary = "Update an existing teacher–offering binding (teacher / subgroup / lessonTypeScope)")
    @PatchMapping("/{id}")
    public ResponseEntity<SubjectAssignmentResponse> update(
        @PathVariable UUID id,
        @Valid
        @RequestBody
        UpdateSubjectAssignmentRequest request
    ) {
        return ResponseEntity.ok(subjectAssignmentApi.update(id, request));
    }

    @Operation(summary = "Remove a teacher from a subject offering")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id
    ) {
        subjectAssignmentApi.delete(id);
        return ResponseEntity.noContent().build();
    }
}