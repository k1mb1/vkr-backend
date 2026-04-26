package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_groups.filters.FindGroupsFilter;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
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

@RequestMapping(
    value = "/api/groups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Student group management")
public interface StudentGroupApi {

    @Operation(summary = "List all groups")
    @GetMapping
    ResponseEntity<Page<StudentGroupPageResponse>> findAll(
        @ParameterObject @ModelAttribute FindGroupsFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "Create a group with students",
        description = """
        Creates a main group and assigns students to it.
        One inner list  → all students placed directly in the group (no subgroups).
        N inner lists → subgroups "groupName/1" … "groupName/N" are auto-created.
        """
    )
    @PostMapping
    ResponseEntity<StudentGroupResponse> create(
        @RequestBody @Valid CreateGroupRequest request
    );

    @Operation(
        summary = "Get group with subgroups and students",
        description = "Returns the main group with all subgroups. Each subgroup contains its students. Direct students (not in any subgroup) are listed under the main group."
    )
    @GetMapping("/{groupId}")
    ResponseEntity<StudentGroupResponse> findGroupWithSubgroups(@PathVariable UUID groupId);

    @Operation(summary = "Rename a group")
    @PatchMapping("/{groupId}")
    ResponseEntity<StudentGroupResponse> update(
        @PathVariable UUID groupId,
        @RequestBody @Valid UpdateGroupRequest request
    );
}
