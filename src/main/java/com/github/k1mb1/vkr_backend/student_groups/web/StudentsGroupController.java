package com.github.k1mb1.vkr_backend.student_groups.web;


import com.github.k1mb1.vkr_backend.student_groups.StudentGroupsApi;
import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    ResponseEntity<Page<StudentGroupListDto>> findAll(
            @ParameterObject @ModelAttribute StudentGroupFilterRequest filter,
            @ParameterObject Pageable pageable
    ){
        return ResponseEntity.status(HttpStatus.OK).body(studentGroupsApi.findAll(filter, pageable));
    }
}
