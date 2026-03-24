package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/subjects",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subjects", description = "Subject management")
public interface SubjectApi {
    @Operation(summary = "List all subjects by teacher")
    @GetMapping("/teachers/{teacherId}")
    @PreAuthorize(
            "@securityService.isSameUser(#teacherId)"
    )
    ResponseEntity<List<SubjectResponse>> findAllByTeacherId(
        @PathVariable UUID teacherId
    );

    @Operation(summary = "List archived subjects by teacher")
    @GetMapping("/teachers/{teacherId}/archived")
    @PreAuthorize(
            "@securityService.isSameUser(#teacherId)"
    )
    ResponseEntity<List<SubjectResponse>> findAllArchivedByTeacherId(
        @PathVariable UUID teacherId
    );

    @Operation(summary = "Create subject")
    @PostMapping
    @PreAuthorize("@securityService.isSameUser(#request.teacherId)")
    ResponseEntity<SubjectResponse> create(
        @RequestBody @Valid CreateSubjectRequest request
    );

    @Operation(summary = "Archive subject")
    @PatchMapping("/{subjectId}/archive")
    ResponseEntity<SubjectResponse> archive(
            @PathVariable UUID subjectId
    );
}
