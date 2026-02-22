package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.subjects.SubjectFilter;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
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
        value = "/api/subjects",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subjects", description = "Subject management")
public interface SubjectApi {

    @Operation(summary = "List all subjects")
    @GetMapping
    ResponseEntity<Page<SubjectResponse>> findAll(
            @ParameterObject @ModelAttribute SubjectFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Get subject by id")
    @GetMapping("/{id}")
    ResponseEntity<SubjectDetailsResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create subject")
    @PostMapping
    ResponseEntity<SubjectResponse> create(@RequestBody @Valid CreateSubjectRequest request);

    @Operation(summary = "Update subject")
    @PatchMapping("/{id}")
    ResponseEntity<SubjectResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateSubjectRequest request);

    @Operation(summary = "Delete subject")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}