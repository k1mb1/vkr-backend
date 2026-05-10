package com.github.k1mb1.vkr_backend.group.web;

import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/groups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Group management")
@RestController
@RequiredArgsConstructor
public class GroupsController {

    final GroupsApi groupsApi;

    @Operation(summary = "Get groups page filtered by name")
    @GetMapping
    public ResponseEntity<Page<GroupPageResponse>> getPage(
            @ParameterObject @ModelAttribute GroupFilter filter,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(groupsApi.getPage(filter, pageable));
    }

    @Operation(summary = "Create a new group with students")
    @PostMapping
    public ResponseEntity<GroupResponse> create(
        @Valid @RequestBody CreateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupsApi.create(request));
    }

    @Operation(summary = "Partially update group")
    @PatchMapping("/{id}")
    public ResponseEntity<GroupResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateGroupRequest request
    ) {
        return ResponseEntity.ok(groupsApi.update(id, request));
    }

    @Operation(summary = "Get group by id")
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupsApi.getById(id));
    }

    @Operation(summary = "Delete group")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        groupsApi.delete(id);
        return ResponseEntity.noContent().build();
    }
}
