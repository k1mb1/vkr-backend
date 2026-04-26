package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.students.filters.FindStudentsFilter;
import com.github.k1mb1.vkr_backend.domain.students.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/students",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Students", description = "Student management")
public interface StudentApi {

    @Operation(summary = "List students")
    @GetMapping
    ResponseEntity<Page<StudentResponse>> findAll(
        @ParameterObject @ModelAttribute FindStudentsFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "Create student")
    @PostMapping
    ResponseEntity<StudentResponse> create(
        @RequestBody @Valid CreateStudentRequest request
    );

    @Operation(summary = "Update student")
    @PutMapping("/{studentId}")
    ResponseEntity<StudentResponse> update(
        @PathVariable UUID studentId,
        @RequestBody @Valid UpdateStudentRequest request
    );

    @Operation(summary = "Delete student")
    @DeleteMapping("/{studentId}")
    ResponseEntity<Void> delete(@PathVariable UUID studentId);
}
