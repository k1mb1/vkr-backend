package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_grades.StudentGradeFilter;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.CreateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpdateStudentGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping(
        value = "/api/grades",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Grades", description = "Grade management")
public interface GradeApi {

    @Operation(summary = "List all grades")
    @GetMapping
    ResponseEntity<Page<StudentGradeResponse>> findAll(
            @ParameterObject @ModelAttribute StudentGradeFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Get grade by id")
    @GetMapping("/{id}")
    ResponseEntity<StudentGradeResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create grade")
    @PostMapping
    ResponseEntity<StudentGradeResponse> create(@RequestBody @Valid CreateStudentGradeRequest request);

    @Operation(summary = "Update grade")
    @PatchMapping("/{id}")
    ResponseEntity<StudentGradeResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateStudentGradeRequest request);

    @Operation(summary = "Delete grade")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}