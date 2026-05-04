package com.github.k1mb1.vkr_backend.education.structure.api;

import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateOrUpdateTeacherRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/teachers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Teachers", description = "Teacher management")
public interface TeacherApi {
    @Operation(summary = "List teachers with optional filter")
    @GetMapping
    ResponseEntity<Page<TeacherResponse>> findAll(
        @ParameterObject @ModelAttribute TeacherFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "Find or create teacher from JWT token (called on login)")
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.isSameUser(#id)")
    ResponseEntity<TeacherResponse> createOrUpdate(
        @PathVariable UUID id,
        @RequestBody @Valid CreateOrUpdateTeacherRequest request
    );
}
