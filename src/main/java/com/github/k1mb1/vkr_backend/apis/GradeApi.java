package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.grades.GradeFilter;
import com.github.k1mb1.vkr_backend.domain.grades.requests.CreateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.requests.UpdateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.responses.GradeResponse;
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
    ResponseEntity<Page<GradeResponse>> findAll(
            @ParameterObject @ModelAttribute GradeFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Get grade by id")
    @GetMapping("/{id}")
    ResponseEntity<GradeResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create grade")
    @PostMapping
    ResponseEntity<GradeResponse> create(@RequestBody @Valid CreateGradeRequest request);

    @Operation(summary = "Update grade")
    @PatchMapping("/{id}")
    ResponseEntity<GradeResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateGradeRequest request);

    @Operation(summary = "Delete grade")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}