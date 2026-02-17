package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_groups.GroupFilter;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateStudentGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupDetailResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
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
        value = "/api/groups",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Group management")
public interface StudentGroupApi {

    @Operation(summary = "List all groups")
    @GetMapping
    ResponseEntity<Page<StudentGroupResponse>> findAll(
            @ParameterObject @ModelAttribute GroupFilter filter,
            @ParameterObject Pageable pageable
    );

    @Operation(summary = "Get group by id")
    @GetMapping("/{id}")
    ResponseEntity<StudentGroupDetailResponse> findById(@PathVariable UUID id);

    @Operation(summary = "Create group")
    @PostMapping
    ResponseEntity<StudentGroupResponse> create(@RequestBody @Valid CreateStudentGroupRequest request);

    @Operation(summary = "Update group")
    @PatchMapping("/{id}")
    ResponseEntity<StudentGroupResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateStudentGroupRequest request);

    @Operation(summary = "Delete group")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id);
}