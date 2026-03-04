package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping(
        value = "/api/subjects/{subjectId}/students",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subject Students", description = "Manage students assigned to subjects")
public interface SubjectStudentApi {

    @Operation(summary = "List students for a subject")
    @GetMapping
    ResponseEntity<Page<StudentResponse>> findStudentsBySubject(
            @PathVariable UUID subjectId,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Assign student to subject")
    @PostMapping("/{studentId}")
    ResponseEntity<Void> addStudentToSubject(@PathVariable UUID subjectId, @PathVariable UUID studentId);

    @Operation(summary = "Assign multiple students to subject")
    @PostMapping
    ResponseEntity<Void> addStudentsToSubject(@PathVariable UUID subjectId, @RequestBody @Valid List<UUID> studentIds);

    @Operation(summary = "Remove student from subject")
    @DeleteMapping("/{studentId}")
    ResponseEntity<Void> removeStudentFromSubject(@PathVariable UUID subjectId, @PathVariable UUID studentId);
}