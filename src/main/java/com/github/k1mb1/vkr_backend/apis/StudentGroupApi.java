package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(
    value = "/api/groups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Groups", description = "Student group management")
public interface StudentGroupApi {

    @Operation(summary = "List all main groups with student count")
    @GetMapping
    ResponseEntity<Page<StudentGroupPageResponse>> findAll(
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "Get group with subgroups and students",
        description = "Returns the main group with all subgroups. Each subgroup contains its students. Direct students (not in any subgroup) are listed under the main group."
    )
    @GetMapping("/{groupId}")
    ResponseEntity<StudentGroupResponse> findGroupWithSubgroups(
        @PathVariable UUID groupId
    );
}
