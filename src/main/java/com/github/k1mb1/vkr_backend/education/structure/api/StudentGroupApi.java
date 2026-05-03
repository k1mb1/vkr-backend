package com.github.k1mb1.vkr_backend.education.structure.api;

import com.github.k1mb1.vkr_backend.education.structure.api.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.GroupPageResponse;
import com.github.k1mb1.vkr_backend.education.structure.api.responses.GroupResponse;
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

@RequestMapping(value = "/api/groups", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Groups", description = "Student group management")
public interface StudentGroupApi {
    @Operation(summary = "List all groups")
    @GetMapping
    ResponseEntity<Page<GroupPageResponse>> findAll(
        @ParameterObject @ModelAttribute StudentGroupFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "Create a group with students")
    @PostMapping
    ResponseEntity<GroupResponse> create(@RequestBody @Valid CreateGroupRequest request);

    @Operation(summary = "Get group with subgroups and students")
    @GetMapping("/{groupId}")
    ResponseEntity<GroupResponse> findGroupWithSubgroups(@PathVariable UUID groupId);

    @Operation(summary = "Rename a group")
    @PatchMapping("/{groupId}")
    ResponseEntity<GroupResponse> update(
        @PathVariable UUID groupId,
        @RequestBody @Valid UpdateGroupRequest request
    );

    @Operation(summary = "Delete a group with all its subgroups and students")
    @DeleteMapping("/{groupId}")
    ResponseEntity<Void> delete(@PathVariable UUID groupId);
}
