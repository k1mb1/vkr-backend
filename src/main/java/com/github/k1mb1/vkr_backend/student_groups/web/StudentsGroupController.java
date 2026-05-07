package com.github.k1mb1.vkr_backend.student_groups.web;

import com.github.k1mb1.vkr_backend.student_groups.StudentGroupsApi;
import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.GroupResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(
    value = "/api/student-groups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Student group management")
@RestController
@RequiredArgsConstructor
public class StudentsGroupController {

    final StudentGroupsApi studentGroupsApi;

    @Operation(summary = "List all groups")
    @GetMapping
    public ResponseEntity<Page<StudentGroupListDto>> findAll(
        @ParameterObject @ModelAttribute StudentGroupFilterRequest filter,
        @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupsApi.findAll(filter, pageable)
        );
    }

    @Operation(summary = "Get group by id")
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupsApi.findById(id)
        );
    }

    @Operation(summary = "Create group with students")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupResponse> create(
        @Valid @RequestBody CreateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            studentGroupsApi.create(request)
        );
    }

    @Operation(summary = "Patch group")
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupResponse> patch(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateGroupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentGroupsApi.patch(id, request)
        );
    }

    @Operation(summary = "Delete group")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studentGroupsApi.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
