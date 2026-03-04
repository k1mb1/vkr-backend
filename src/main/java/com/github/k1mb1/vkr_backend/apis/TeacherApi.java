package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherFilter;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.CreateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.requests.UpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
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
        value = "/api/teachers",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Teachers", description = "Teacher management")
public interface TeacherApi {

    @Operation(summary = "List all teachers")
    @GetMapping
    ResponseEntity<Page<TeacherResponse>> findAll(
            @ParameterObject @ModelAttribute TeacherFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "List all teachers by subject")
    @GetMapping("/subjects/{subjectId}")
    ResponseEntity<List<TeacherResponse>> findAllBySubjectId(@PathVariable UUID subjectId);

    @Operation(summary = "Get teacher by id")
    @GetMapping("/{id}")
    ResponseEntity<TeacherDetailsResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create teacher")
    @PostMapping
    ResponseEntity<TeacherResponse> create(@RequestBody @Valid CreateTeacherRequest request);

    @Operation(summary = "Update teacher")
    @PatchMapping("/{id}")
    ResponseEntity<TeacherResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateTeacherRequest request);

    @Operation(summary = "Delete teacher")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}