package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
        value = "/api/subjects/{subjectId}/teachers",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subject Teachers", description = "Manage teachers assigned to subjects")
public interface SubjectTeacherApi {

    @Operation(summary = "List teachers for a subject")
    @GetMapping
    ResponseEntity<Page<TeacherResponse>> findTeachersBySubject(
            @PathVariable UUID subjectId,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Assign teacher to subject")
    @PostMapping("/{teacherId}")
    ResponseEntity<Void> addTeacherToSubject(@PathVariable UUID subjectId, @PathVariable UUID teacherId);

    @Operation(summary = "Remove teacher from subject")
    @DeleteMapping("/{teacherId}")
    ResponseEntity<Void> removeTeacherFromSubject(@PathVariable UUID subjectId, @PathVariable UUID teacherId);
}