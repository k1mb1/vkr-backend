package com.github.k1mb1.vkr_backend.education.subjects.api;

import com.github.k1mb1.vkr_backend.education.subjects.api.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.GroupAttachmentResponse;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/subjects", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subjects", description = "Subject management")
public interface SubjectApi {
    @Operation(summary = "List subjects by teacher")
    @GetMapping("/teachers/{teacherId}")
    @PreAuthorize("@securityService.isSameUser(#teacherId)")
    ResponseEntity<List<SubjectResponse>> findAllByTeacherId(
        @PathVariable UUID teacherId,
        @ParameterObject @ModelAttribute FindSubjectsFilter filter
    );

    @Operation(summary = "Create subject")
    @PostMapping
    @PreAuthorize("@securityService.isSameUser(#request.teacherId)")
    ResponseEntity<SubjectResponse> create(@RequestBody @Valid CreateSubjectRequest request);

    @Operation(summary = "Update subject metadata")
    @PatchMapping("/{subjectId}")
    ResponseEntity<SubjectResponse> update(@PathVariable UUID subjectId, @RequestBody @Valid UpdateSubjectRequest request);

    @Operation(summary = "Attach full group to subject")
    @PostMapping("/{subjectId}/groups/{groupId}")
    ResponseEntity<GroupAttachmentResponse> attachGroup(@PathVariable UUID subjectId, @PathVariable UUID groupId);

    @Operation(summary = "Remove subject")
    @DeleteMapping("/{subjectId}")
    ResponseEntity<Void> remove(@PathVariable UUID subjectId);
}
