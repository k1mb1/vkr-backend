package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectsApi;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import com.github.k1mb1.vkr_backend.subject.web.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectPageResponse;
import com.github.k1mb1.vkr_backend.subject.web.responses.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/subjects",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subjects", description = "Subject management")
@RestController
@RequiredArgsConstructor
public class SubjectsController {

    final SubjectsApi subjectsApi;

    @Operation(summary = "Get subjects page filtered by name")
    @GetMapping
    public ResponseEntity<Page<SubjectPageResponse>> getPage(
        @ModelAttribute SubjectFilter filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(subjectsApi.getPage(filter, pageable));
    }

    @Operation(summary = "Partially update subject")
    @PatchMapping("/{id}")
    public ResponseEntity<SubjectResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateSubjectRequest request
    ) {
        return ResponseEntity.ok(subjectsApi.update(id, request));
    }
}
